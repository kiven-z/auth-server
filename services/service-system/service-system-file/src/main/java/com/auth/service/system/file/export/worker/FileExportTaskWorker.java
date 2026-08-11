package com.auth.service.system.file.export.worker;

import com.auth.module.file.export.task.FileExportTaskArtifact;
import com.auth.service.system.file.config.properties.FileExportTaskProperties;
import com.auth.service.system.file.mapper.FileExportTaskMapper;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.model.enums.FileExportAttemptResult;
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
		try {
			FileExportTaskRunResult runResult = taskRunner.run(task);
			attemptResult = runResult.getAttemptResult();
			processedRows = runResult.getProcessedRows();
			artifact = runResult.getArtifact();
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
			attemptRecorder.append(taskId, attemptNo, startedAt, Instant.now(), attemptResult, processedRows, errorCode,
					errorMessage);
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
