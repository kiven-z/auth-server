package com.auth.service.system.file.export.worker;

import com.auth.module.file.export.task.FileExportTaskArtifact;
import com.auth.service.system.file.config.properties.FileExportTaskProperties;
import com.auth.service.system.file.export.notify.FileExportTaskNotifier;
import com.auth.service.system.file.mapper.FileExportTaskMapper;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.model.enums.FileExportAttemptResult;
import com.auth.service.system.file.model.enums.FileExportTaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

/**
 * 文件异步导出任务 Worker：认领候选任务并提交执行池
 *
 * @author Bunny
 */
@Slf4j
@Component
public class FileExportTaskWorker {

	private final FileExportTaskMapper fileExportTaskMapper;

	private final FileExportTaskProperties properties;

	private final FileExportTaskRunner taskRunner;

	private final FileExportTaskFailureHandler failureHandler;

	private final FileExportAttemptRecorder attemptRecorder;

	private final FileExportTaskNotifier taskNotifier;

	private final Executor fileExportTaskExecutor;

	private final Semaphore inFlight;

	public FileExportTaskWorker(FileExportTaskMapper fileExportTaskMapper, FileExportTaskProperties properties,
			FileExportTaskRunner taskRunner, FileExportTaskFailureHandler failureHandler,
			FileExportAttemptRecorder attemptRecorder, FileExportTaskNotifier taskNotifier,
			@Qualifier("fileExportTaskExecutor") Executor fileExportTaskExecutor) {
		this.fileExportTaskMapper = fileExportTaskMapper;
		this.properties = properties;
		this.taskRunner = taskRunner;
		this.failureHandler = failureHandler;
		this.attemptRecorder = attemptRecorder;
		this.taskNotifier = taskNotifier;
		this.fileExportTaskExecutor = fileExportTaskExecutor;
		this.inFlight = new Semaphore(properties.getMaxConcurrency());
	}

	/**
	 * 执行一轮任务拉取与提交
	 */
	public void executeRound() {
		int slots = inFlight.availablePermits();
		if (slots <= 0) {
			return;
		}
		Instant now = Instant.now();
		int limit = Math.min(properties.getClaimBatchSize(), slots);
		List<Long> candidateIds = fileExportTaskMapper.selectClaimCandidateIds(now, limit);
		for (Long taskId : candidateIds) {
			submitClaimed(taskId);
		}
	}

	/**
	 * 抢占租约并提交执行池；无空闲槽或抢占失败则跳过
	 * @param taskId 任务ID
	 */
	private void submitClaimed(Long taskId) {
		if (!inFlight.tryAcquire()) {
			return;
		}
		String workerId = "fe-" + UUID.randomUUID().toString().replace("-", "");
		Instant leaseUntil = Instant.now().plus(properties.getLeaseSeconds(), ChronoUnit.SECONDS);
		int claimed = fileExportTaskMapper.claimTask(taskId, workerId, leaseUntil, Instant.now());
		if (claimed < 1) {
			inFlight.release();
			return;
		}
		try {
			fileExportTaskExecutor.execute(() -> runClaimedTask(taskId, workerId));
		}
		catch (RejectedExecutionException exception) {
			inFlight.release();
			log.error("File export executor rejected task: taskId={}, workerId={}", taskId, workerId, exception);
		}
	}

	/**
	 * 在工作线程中执行已认领任务并释放并发槽
	 * @param taskId 任务ID
	 * @param workerId 本次执行的租约持有者标识
	 */
	private void runClaimedTask(Long taskId, String workerId) {
		try {
			processTask(taskId, workerId);
		}
		catch (RuntimeException exception) {
			log.error("File export worker crashed: taskId={}, workerId={}", taskId, workerId, exception);
		}
		finally {
			inFlight.release();
		}
	}

	/**
	 * 处理单个已认领任务
	 * @param taskId 任务ID
	 * @param workerId 本次执行的租约持有者标识
	 */
	private void processTask(Long taskId, String workerId) {
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
		String artifactFileName = null;
		try {
			FileExportTaskRunResult runResult = taskRunner.run(task, workerId);
			attemptResult = runResult.getAttemptResult();
			processedRows = runResult.getProcessedRows();
			artifact = runResult.getArtifact();
			if (artifact != null) {
				artifactFileName = artifact.getFileName();
			}
		}
		catch (Exception exception) {
			attemptResult = failureHandler.handle(task, exception, workerId);
			if (attemptResult == FileExportAttemptResult.FAILED) {
				errorCode = exception.getClass().getSimpleName();
				errorMessage = exception.getMessage();
			}
		}
		finally {
			deleteArtifactQuietly(artifact);
			int attemptNo = Objects.requireNonNullElse(task.getRetryCount(), 0) + 1;
			attemptRecorder.append(FileExportAttemptAppendCommand.builder()
				.taskId(taskId)
				.workerId(workerId)
				.attemptNo(attemptNo)
				.startedAt(startedAt)
				.finishedAt(Instant.now())
				.result(attemptResult)
				.processedRows(processedRows)
				.errorCode(errorCode)
				.errorMessage(errorMessage)
				.build());
			dispatchTerminalNotification(taskId, attemptResult, artifactFileName, errorMessage);
		}
	}

	/**
	 * 终态通知：成功或不可重试失败；跳过重试中与取消
	 * @param taskId 任务ID
	 * @param attemptResult 本次尝试结果
	 * @param artifactFileName 产物文件名
	 * @param errorMessage 失败摘要
	 */
	private void dispatchTerminalNotification(Long taskId, FileExportAttemptResult attemptResult,
			String artifactFileName, String errorMessage) {
		FileExportTaskEntity latest = fileExportTaskMapper.selectById(taskId);
		if (latest == null) {
			return;
		}
		if (attemptResult == FileExportAttemptResult.SUCCESS) {
			taskNotifier.notifySuccess(latest, artifactFileName);
			return;
		}
		if (attemptResult == FileExportAttemptResult.FAILED
				&& FileExportTaskStatus.FAILED.name().equals(latest.getStatus())) {
			taskNotifier.notifyTerminalFailure(latest, errorMessage);
		}
	}

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

}
