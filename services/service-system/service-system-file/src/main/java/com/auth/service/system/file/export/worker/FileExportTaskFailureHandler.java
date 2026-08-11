package com.auth.service.system.file.export.worker;

import java.time.Instant;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.auth.module.file.exception.ExportValidationException;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.config.properties.FileExportTaskProperties;
import com.auth.service.system.file.mapper.FileExportTaskMapper;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.model.enums.FileExportAttemptResult;
import com.auth.service.system.file.model.enums.FileExportTaskStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 导出任务失败结局：取消 / 退避重试 / 终态失败
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class FileExportTaskFailureHandler {

	private static final long RETRY_MAX_DELAY_SECONDS = 900L;

	private final FileExportTaskMapper fileExportTaskMapper;

	private final FileExportTaskProperties properties;

	/**
	 * 处理执行异常并回写任务状态
	 * @param task 任务
	 * @param exception 执行异常
	 * @return attempt 结果（取消或失败）
	 */
	public FileExportAttemptResult handle(FileExportTaskEntity task, Exception exception) {
		Long taskId = task.getId();
		if (isCancelRequested(taskId)) {
			fileExportTaskMapper.markCancelled(taskId, FileExportWorkerConstants.WORKER_ID, Instant.now());
			return FileExportAttemptResult.CANCELLED;
		}

		String errorCode = exception.getClass().getSimpleName();
		String errorMessage = exception.getMessage();
		boolean errorRetryable = isRetryableException(exception);
		int retryRows = errorRetryable ? markRetryPending(task, errorCode, errorMessage) : 0;
		if (retryRows < 1) {
			fileExportTaskMapper.markFailed(taskId, FileExportWorkerConstants.WORKER_ID, errorCode, errorMessage,
					Instant.now());
		}
		log.warn("File export task failed: taskId={}, errorCode={}, errorMessage={}", taskId, errorCode, errorMessage,
				exception);
		return FileExportAttemptResult.FAILED;
	}

	/**
	 * 检查任务是否被取消
	 * @param taskId 任务ID
	 * @return 是否被取消
	 */
	private boolean isCancelRequested(Long taskId) {
		FileExportTaskEntity latest = fileExportTaskMapper.selectById(taskId);
		return latest != null && FileExportTaskStatus.CANCELLING.name().equals(latest.getStatus());
	}

	/**
	 * 标记重试
	 * @param task 任务实体
	 * @param errorCode 错误码
	 * @param errorMessage 错误信息
	 * @return 重试行数
	 */
	private int markRetryPending(FileExportTaskEntity task, String errorCode, String errorMessage) {
		Integer retryCount = Objects.requireNonNullElse(task.getRetryCount(), 0);
		Integer maxRetry = Objects.requireNonNullElse(task.getMaxRetry(), 0);
		if (retryCount >= maxRetry) {
			return 0;
		}
		Instant nextRetryAt = buildNextRetryAt(retryCount);
		return fileExportTaskMapper.markRetryPending(task.getId(), FileExportWorkerConstants.WORKER_ID, errorCode,
				errorMessage, nextRetryAt, Instant.now());
	}

	/**
	 * 构建下次重试时间
	 * @param retryCount 重试次数
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
	 * 检查异常是否可重试
	 * @param exception 异常
	 * @return 是否可重试
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
