package com.auth.service.system.file.export.worker;

import com.auth.module.file.export.task.FileExportTaskArtifact;
import com.auth.service.system.file.config.properties.FileExportTaskProperties;
import com.auth.service.system.file.export.notify.FileExportTaskNotifier;
import com.auth.service.system.file.mapper.FileExportTaskMapper;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.model.enums.FileExportAttemptResult;
import com.auth.service.system.file.model.enums.FileExportTaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * 文件异步导出任务 Worker：认领候选任务并编排执行
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class FileExportTaskWorker {

	private final FileExportTaskMapper fileExportTaskMapper;

	private final FileExportTaskProperties properties;

	private final FileExportTaskRunner taskRunner;

	private final FileExportTaskFailureHandler failureHandler;

	private final FileExportAttemptRecorder attemptRecorder;

	private final FileExportTaskNotifier taskNotifier;

	/**
	 * 执行一轮任务拉取与处理
	 */
	public void executeRound() {
		Instant now = Instant.now();
		List<Long> candidateIds = fileExportTaskMapper.selectClaimCandidateIds(now, properties.getClaimBatchSize());
		for (Long taskId : candidateIds) {
			Instant leaseUntil = Instant.now().plus(properties.getLeaseSeconds(), ChronoUnit.SECONDS);
			int claimed = fileExportTaskMapper.claimTask(taskId, FileExportWorkerConstants.WORKER_ID, leaseUntil,
					Instant.now());
			if (claimed < 1) {
				continue;
			}
			processTask(taskId);
		}
	}

	/**
	 * 处理单个已认领任务
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
		String artifactFileName = null;
		try {
			FileExportTaskRunResult runResult = taskRunner.run(task);
			attemptResult = runResult.getAttemptResult();
			processedRows = runResult.getProcessedRows();
			artifact = runResult.getArtifact();
			if (artifact != null) {
				artifactFileName = artifact.getFileName();
			}
		}
		catch (Exception exception) {
			attemptResult = failureHandler.handle(task, exception);
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
