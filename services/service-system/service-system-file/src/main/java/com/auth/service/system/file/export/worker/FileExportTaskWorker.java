package com.auth.service.system.file.export.worker;

import com.auth.common.core.constants.BatchSizes;
import com.auth.module.file.api.model.dto.FileUploadResultDTO;
import com.auth.module.file.api.policy.FileBizType;
import com.auth.module.file.exception.ExportValidationException;
import com.auth.module.file.export.scope.FileExportScopeSnapshots;
import com.auth.module.file.export.task.FileExportTaskArtifact;
import com.auth.module.file.export.task.FileExportTaskContext;
import com.auth.module.security.autoconfigure.pipeline.authenticate.AuthProfileSecurityContextPopulator;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.config.properties.FileExportTaskProperties;
import com.auth.service.system.file.export.executor.FileExportTaskExecutor;
import com.auth.service.system.file.export.listener.DatabaseProgressListener;
import com.auth.service.system.file.mapper.FileExportAttemptMapper;
import com.auth.service.system.file.mapper.FileExportTaskMapper;
import com.auth.service.system.file.model.entity.FileExportAttemptEntity;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.model.enums.FileExportAttemptResult;
import com.auth.service.system.file.model.enums.FileExportTaskStatus;
import com.auth.service.system.file.model.form.FileUploadForm;
import com.auth.service.system.file.model.value.PathMultipartFile;
import com.auth.service.system.file.service.FileUploadService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * 文件异步导出任务 Worker
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class FileExportTaskWorker {

	private static final int ATTEMPT_INSERT_MAX_RETRIES = 3;

	private static final String WORKER_ID = "file-export-worker";

	private static final long RETRY_MAX_DELAY_SECONDS = 900L;

	private final FileExportTaskMapper fileExportTaskMapper;

	private final FileExportAttemptMapper fileExportAttemptMapper;

	private final FileUploadService fileUploadService;

	private final FileExportTaskExecutor fileExportTaskExecutor;

	private final FileExportTaskProperties properties;

	private final AuthProfileSecurityContextPopulator securityContextPopulator = new AuthProfileSecurityContextPopulator();

	/**
	 * 执行一轮任务拉取与处理
	 */
	public void executeRound() {
		Instant now = Instant.now();
		List<Long> candidateIds = fileExportTaskMapper.selectClaimCandidateIds(now, properties.getClaimBatchSize());
		for (Long taskId : candidateIds) {
			Instant leaseUntil = Instant.now().plus(properties.getLeaseSeconds(), ChronoUnit.SECONDS);
			int claimed = fileExportTaskMapper.claimTask(taskId, WORKER_ID, leaseUntil, Instant.now());
			if (claimed < 1) {
				continue;
			}
			processTask(taskId);
		}
	}

	/**
	 * 处理单个任务
	 * @param taskId 任务ID
	 */
	private void processTask(Long taskId) {
		Instant startedAt = Instant.now();
		FileExportTaskEntity task = fileExportTaskMapper.selectById(taskId);
		if (task == null) {
			return;
		}

		FileExportAttemptResult attemptResult = FileExportAttemptResult.FAILED;
		String errorCode = null;
		String errorMessage = null;
		long processedRows = 0L;
		FileExportTaskArtifact artifact = null;
		try {
			DatabaseProgressListener progressListener = new DatabaseProgressListener(fileExportTaskMapper, properties,
					taskId, WORKER_ID);
			if (progressListener.isCancelRequested()) {
				fileExportTaskMapper.markCancelled(taskId, WORKER_ID, Instant.now());
				attemptResult = FileExportAttemptResult.CANCELLED;
				return;
			}

			AuthProfile profile = FileExportScopeSnapshots.requireAuthProfile(task.getScopeSnapshotJson());
			if (task.getCreatedBy() != null && !task.getCreatedBy().equals(profile.getUserId())) {
				throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID, "scopeSnapshotUser");
			}
			securityContextPopulator.populate(profile);
			try {
				FileExportTaskContext context = FileExportTaskContext.builder()
					.taskId(task.getId())
					.bizType(task.getBizType())
					.exportFormat(task.getExportFormat())
					.querySnapshotJson(task.getQuerySnapshotJson())
					.columnSnapshotJson(task.getColumnSnapshotJson())
					.scopeSnapshotJson(task.getScopeSnapshotJson())
					.fetchService(task.getFetchService())
					.fetchPath(task.getFetchPath())
					.createdBy(task.getCreatedBy())
					.createdAt(task.getCreatedAt())
					.fetchPageSize(BatchSizes.SIZE_1000)
					.maxRows(properties.getMaxRows())
					.build();
				artifact = fileExportTaskExecutor.executeTask(context, progressListener);
				processedRows = artifact.getRowCount();
				if (progressListener.isCancelRequested()) {
					deleteArtifactQuietly(artifact);
					artifact = null;
					fileExportTaskMapper.markCancelled(taskId, WORKER_ID, Instant.now());
					attemptResult = FileExportAttemptResult.CANCELLED;
					return;
				}

				Long fileRecordId = uploadArtifact(task, artifact);
				Instant expireAt = Instant.now().plus(properties.getArtifactExpireDays(), ChronoUnit.DAYS);
				fileExportTaskMapper.markSuccess(taskId, WORKER_ID, fileRecordId, expireAt, Instant.now());
				attemptResult = FileExportAttemptResult.SUCCESS;
			}
			finally {
				SecurityContextHolder.clearContext();
			}
		}
		catch (Exception exception) {
			if (isCancelRequested(taskId)) {
				fileExportTaskMapper.markCancelled(taskId, WORKER_ID, Instant.now());
				attemptResult = FileExportAttemptResult.CANCELLED;
			}
			else {
				errorCode = exception.getClass().getSimpleName();
				errorMessage = exception.getMessage();
				boolean errorRetryable = isRetryableException(exception);
				int retryRows = errorRetryable ? markRetryPending(task, errorCode, errorMessage) : 0;
				if (retryRows < 1) {
					fileExportTaskMapper.markFailed(taskId, WORKER_ID, errorCode, errorMessage, Instant.now());
				}
			}
			log.warn("File export task failed: taskId={}, errorCode={}, errorMessage={}", taskId, errorCode,
					errorMessage, exception);
		}
		finally {
			deleteArtifactQuietly(artifact);
			appendAttempt(taskId, startedAt, Instant.now(), attemptResult, processedRows, errorCode, errorMessage);
		}
	}

	/**
	 * 写入执行尝试记录
	 * @param taskId 任务ID
	 * @param startedAt 开始时间
	 * @param finishedAt 结束时间
	 * @param result 执行结果
	 * @param processedRows 处理行数
	 * @param errorCode 错误码
	 * @param errorMessage 错误信息
	 */
	private void appendAttempt(Long taskId, Instant startedAt, Instant finishedAt, FileExportAttemptResult result,
			long processedRows, String errorCode, String errorMessage) {
		for (int attempt = 1; attempt <= ATTEMPT_INSERT_MAX_RETRIES; attempt++) {
			FileExportAttemptEntity attemptEntity = new FileExportAttemptEntity();
			attemptEntity.setTaskId(taskId);
			attemptEntity.setAttemptNo(resolveNextAttemptNo(taskId));
			attemptEntity.setWorkerId(WORKER_ID);
			attemptEntity.setResult(result.name());
			attemptEntity.setProcessedRows(processedRows);
			attemptEntity.setErrorCode(errorCode);
			attemptEntity.setErrorMessage(errorMessage);
			attemptEntity.setStartedAt(startedAt);
			attemptEntity.setFinishedAt(finishedAt);
			try {
				fileExportAttemptMapper.insert(attemptEntity);
				return;
			}
			catch (DuplicateKeyException exception) {
				log.warn("Retry append export attempt due to duplicate attempt_no: taskId={}, retry={}", taskId,
						attempt);
			}
		}
		log.error("Append export attempt failed after retries: taskId={}", taskId);
	}

	/**
	 * 读取下一个尝试序号
	 * @param taskId 任务ID
	 * @return 下一个序号（从1开始）
	 */
	private int resolveNextAttemptNo(Long taskId) {
		var wrapper = Wrappers.<FileExportAttemptEntity>lambdaQuery()
			.eq(FileExportAttemptEntity::getTaskId, taskId)
			.orderByDesc(FileExportAttemptEntity::getAttemptNo)
			.last("LIMIT 1");
		FileExportAttemptEntity latest = fileExportAttemptMapper.selectOne(wrapper);
		return latest == null || latest.getAttemptNo() == null ? 1 : latest.getAttemptNo() + 1;
	}

	/**
	 * 上传导出产物并写入文件记录
	 * @param task 任务
	 * @param artifact 导出产物
	 * @return 文件记录ID
	 */
	private Long uploadArtifact(FileExportTaskEntity task, FileExportTaskArtifact artifact) {
		Path tempFile = artifact.getFilePath();
		if (tempFile == null || !Files.exists(tempFile)) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE, "exportArtifact");
		}
		PathMultipartFile file = new PathMultipartFile("file", artifact.getFileName(), artifact.getContentType(),
				tempFile);
		FileUploadForm form = new FileUploadForm();
		form.setFile(file);
		form.setBizType(FileBizType.INTERNAL_EXPORT.getCode());
		form.setBizId(String.valueOf(task.getId()));
		form.setIsPrivate(true);
		form.setRemark("async-export-task");

		FileUploadResultDTO result = fileUploadService.upload(form);
		Long fileRecordId = result.getId();
		if (fileRecordId == null) {
			throw new SystemBusinessException(SystemCommonResultCode.OPERATION_FAILED, "fileRecordId");
		}
		return fileRecordId;
	}

	/**
	 * 清理导出临时文件
	 * @param artifact 导出产物
	 */
	private void deleteArtifactQuietly(FileExportTaskArtifact artifact) {
		if (artifact == null || artifact.getFilePath() == null) {
			return;
		}
		try {
			Files.deleteIfExists(artifact.getFilePath());
		}
		catch (Exception exception) {
			log.warn("Failed to delete export temp file: path={}", artifact.getFilePath(), exception);
		}
	}

	/**
	 * 判断任务是否已请求取消
	 * @param taskId 任务ID
	 * @return 已请求则 true
	 */
	private boolean isCancelRequested(Long taskId) {
		FileExportTaskEntity latest = fileExportTaskMapper.selectById(taskId);
		return latest != null && FileExportTaskStatus.CANCELLING.name().equals(latest.getStatus());
	}

	/**
	 * 将失败任务按退避策略回退为待处理
	 * @param task 任务
	 * @param errorCode 错误码
	 * @param errorMessage 错误信息
	 * @return 影响行数
	 */
	private int markRetryPending(FileExportTaskEntity task, String errorCode, String errorMessage) {
		Integer retryCount = Objects.requireNonNullElse(task.getRetryCount(), 0);
		Integer maxRetry = Objects.requireNonNullElse(task.getMaxRetry(), 0);
		if (retryCount >= maxRetry) {
			return 0;
		}
		Instant nextRetryAt = buildNextRetryAt(retryCount);
		return fileExportTaskMapper.markRetryPending(task.getId(), WORKER_ID, errorCode, errorMessage, nextRetryAt,
				Instant.now());
	}

	/**
	 * 计算下次重试时间（指数退避）
	 * @param retryCount 当前重试次数
	 * @return 下次重试时间
	 */
	private Instant buildNextRetryAt(int retryCount) {
		long baseDelaySeconds = Math.max(1, properties.getRetryBaseDelaySeconds());
		long maxDelaySeconds = Math.max(baseDelaySeconds, RETRY_MAX_DELAY_SECONDS);
		int shift = Math.max(0, Math.min(retryCount, 20));
		long delaySeconds = Math.min(maxDelaySeconds, baseDelaySeconds * (1L << shift));
		return Instant.now().plusSeconds(delaySeconds);
	}

	/**
	 * 判断异常是否允许自动重试
	 * @param exception 执行异常
	 * @return 可重试则 true
	 */
	private boolean isRetryableException(Exception exception) {
		if (exception instanceof ExportValidationException) {
			return false;
		}
		if (exception instanceof SystemBusinessException businessException) {
			return businessException.getResultCode() != SystemCommonResultCode.PARAM_REQUIRED
					&& businessException.getResultCode() != SystemCommonResultCode.DATA_INVALID
					&& businessException.getResultCode() != SystemCommonResultCode.GRANT_REFERENCE_INVALID
					&& businessException.getResultCode() != SystemCommonResultCode.DATA_UNAVAILABLE;
		}
		return true;
	}

}
