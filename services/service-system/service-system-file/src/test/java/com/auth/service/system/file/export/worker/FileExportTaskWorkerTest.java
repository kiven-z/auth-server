package com.auth.service.system.file.export.worker;

import com.auth.module.file.api.model.dto.FileUploadResultDTO;
import com.auth.module.file.exception.ExportValidationException;
import com.auth.module.file.export.scope.FileExportScopeSnapshots;
import com.auth.module.file.export.executor.AsyncPagedExportExecutor;
import com.auth.module.file.export.task.FileExportTaskArtifact;
import com.auth.module.file.export.task.FileExportTaskContext;
import com.auth.module.security.autoconfigure.pipeline.authenticate.AuthProfileSecurityContextPopulator;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.service.system.file.config.properties.FileExportTaskProperties;
import com.auth.service.system.file.export.remote.RemotePageFetcher;
import com.auth.service.system.file.mapper.FileExportAttemptMapper;
import com.auth.service.system.file.mapper.FileExportTaskMapper;
import com.auth.service.system.file.model.entity.FileExportAttemptEntity;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.service.FileUploadService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link FileExportTaskWorker} 单元测试
 *
 * @author Bunny
 */
@DisplayName("FileExportTaskWorker 任务执行")
@ExtendWith(MockitoExtension.class)
class FileExportTaskWorkerTest {

	@Mock
	private FileExportTaskMapper fileExportTaskMapper;

	@Mock
	private FileExportAttemptMapper fileExportAttemptMapper;

	@Mock
	private FileUploadService fileUploadService;

	@Mock
	private RemotePageFetcher remotePageFetcher;

	@Mock
	private AsyncPagedExportExecutor asyncPagedExportExecutor;

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("executeRound：任务成功时写入 SUCCESS 尝试记录并恢复快照上下文")
	void executeRoundWritesSuccessAttempt() {
		FileExportTaskProperties properties = newProperties();
		FileExportTaskWorker worker = newWorker(properties);

		FileExportTaskEntity task = newTask(101L);
		when(fileExportTaskMapper.selectClaimCandidateIds(any(), eq(5))).thenReturn(List.of(101L));
		when(fileExportTaskMapper.claimTask(eq(101L), eq("file-export-worker"), any(), any())).thenReturn(1);
		when(fileExportTaskMapper.selectById(101L)).thenReturn(task);
		AtomicReference<AuthProfile> profileDuringExport = new AtomicReference<>();
		AtomicReference<Path> artifactPath = new AtomicReference<>();
		when(asyncPagedExportExecutor.export(any(FileExportTaskContext.class), any(), any())).thenAnswer(invocation -> {
			profileDuringExport.set(SecurityUserUtils.currentAuthProfile());
			Path tempPath = writeArtifactFile();
			artifactPath.set(tempPath);
			return FileExportTaskArtifact.builder()
				.fileName("file_export.xlsx")
				.contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
				.filePath(tempPath)
				.rowCount(12)
				.build();
		});
		FileUploadResultDTO uploadResult = new FileUploadResultDTO();
		uploadResult.setId(7001L);
		when(fileUploadService.upload(any())).thenReturn(uploadResult);

		worker.executeRound();

		verify(fileExportTaskMapper).markSuccess(eq(101L), eq("file-export-worker"), eq(7001L), any(), any());
		verify(fileExportTaskMapper, never()).markFailed(anyLong(), anyString(), anyString(), anyString(), any());
		verify(fileExportTaskMapper, never()).markCancelled(anyLong(), anyString(), any());
		verify(fileExportTaskMapper, never()).markRetryPending(anyLong(), anyString(), anyString(), anyString(), any(),
				any());

		ArgumentCaptor<FileExportAttemptEntity> captor = ArgumentCaptor.forClass(FileExportAttemptEntity.class);
		verify(fileExportAttemptMapper).insert(captor.capture());
		assertThat(captor.getValue().getResult()).isEqualTo("SUCCESS");
		assertThat(captor.getValue().getProcessedRows()).isEqualTo(12L);
		assertThat(profileDuringExport.get()).isNotNull();
		assertThat(profileDuringExport.get().getUserId()).isEqualTo(9001L);
		assertThat(profileDuringExport.get().getDeptScope()).isNotNull();
		assertThat(profileDuringExport.get().getDeptScope().getScopeType())
			.isEqualTo(DataScopeStorageType.DEPT_AND_CHILD);
		assertThat(SecurityUserUtils.currentAuthProfile()).isNull();
		assertThat(artifactPath.get()).isNotNull();
		assertThat(Files.exists(artifactPath.get())).isFalse();
	}

	@Test
	@DisplayName("executeRound：任务失败且可重试时回退为 PENDING")
	void executeRoundMovesToPendingWhenRetryableFailure() {
		FileExportTaskProperties properties = newProperties();
		FileExportTaskWorker worker = newWorker(properties);

		FileExportTaskEntity task = newTask(102L);
		task.setRetryCount(0);
		task.setMaxRetry(3);
		when(fileExportTaskMapper.selectClaimCandidateIds(any(), eq(5))).thenReturn(List.of(102L));
		when(fileExportTaskMapper.claimTask(eq(102L), eq("file-export-worker"), any(), any())).thenReturn(1);
		when(fileExportTaskMapper.selectById(102L)).thenReturn(task);
		when(asyncPagedExportExecutor.export(any(FileExportTaskContext.class), any(), any()))
			.thenThrow(new IllegalStateException("boom"));
		when(fileExportTaskMapper.markRetryPending(eq(102L), eq("file-export-worker"), eq("IllegalStateException"),
				eq("boom"), any(), any()))
			.thenReturn(1);

		worker.executeRound();

		verify(fileExportTaskMapper).markRetryPending(eq(102L), eq("file-export-worker"), eq("IllegalStateException"),
				eq("boom"), any(), any());
		verify(fileExportTaskMapper, never()).markFailed(anyLong(), anyString(), anyString(), anyString(), any());
		verify(fileExportTaskMapper, never()).markCancelled(anyLong(), anyString(), any());
		assertThat(SecurityUserUtils.currentAuthProfile()).isNull();

		ArgumentCaptor<FileExportAttemptEntity> captor = ArgumentCaptor.forClass(FileExportAttemptEntity.class);
		verify(fileExportAttemptMapper).insert(captor.capture());
		assertThat(captor.getValue().getResult()).isEqualTo("FAILED");
	}

	@Test
	@DisplayName("executeRound：导出校验失败时标记 FAILED 且不自动重试")
	void executeRoundMarksFailedWhenExportValidationException() {
		FileExportTaskProperties properties = newProperties();
		FileExportTaskWorker worker = newWorker(properties);

		FileExportTaskEntity task = newTask(106L);
		task.setRetryCount(0);
		task.setMaxRetry(3);
		when(fileExportTaskMapper.selectClaimCandidateIds(any(), eq(5))).thenReturn(List.of(106L));
		when(fileExportTaskMapper.claimTask(eq(106L), eq("file-export-worker"), any(), any())).thenReturn(1);
		when(fileExportTaskMapper.selectById(106L)).thenReturn(task);
		when(asyncPagedExportExecutor.export(any(FileExportTaskContext.class), any(), any()))
			.thenThrow(ExportValidationException.rowLimitExceeded(100L, 101L));

		worker.executeRound();

		verify(fileExportTaskMapper).markFailed(eq(106L), eq("file-export-worker"), eq("ExportValidationException"),
				anyString(), any());
		verify(fileExportTaskMapper, never()).markRetryPending(anyLong(), anyString(), anyString(), anyString(), any(),
				any());
		assertThat(SecurityUserUtils.currentAuthProfile()).isNull();
	}

	@Test
	@DisplayName("executeRound：非法权限快照且重试耗尽时标记 FAILED")
	void executeRoundMarksFailedWhenScopeSnapshotInvalid() {
		FileExportTaskProperties properties = newProperties();
		FileExportTaskWorker worker = newWorker(properties);

		FileExportTaskEntity task = newTask(104L);
		task.setScopeSnapshotJson("{\"permVersion\":1}");
		task.setRetryCount(3);
		task.setMaxRetry(3);
		when(fileExportTaskMapper.selectClaimCandidateIds(any(), eq(5))).thenReturn(List.of(104L));
		when(fileExportTaskMapper.claimTask(eq(104L), eq("file-export-worker"), any(), any())).thenReturn(1);
		when(fileExportTaskMapper.selectById(104L)).thenReturn(task);

		worker.executeRound();

		verify(asyncPagedExportExecutor, never()).export(any(), any(), any());
		verify(fileExportTaskMapper).markFailed(eq(104L), eq("file-export-worker"), eq("IllegalArgumentException"),
				eq("scopeSnapshotJson must contain valid userId"), any());
		verify(fileExportTaskMapper, never()).markRetryPending(anyLong(), anyString(), anyString(), anyString(), any(),
				any());
		assertThat(SecurityUserUtils.currentAuthProfile()).isNull();
	}

	@Test
	@DisplayName("executeRound：执行前已收到取消请求时标记 CANCELLED")
	void executeRoundMarksCancelledBeforeExportRun() {
		FileExportTaskProperties properties = newProperties();
		FileExportTaskWorker worker = newWorker(properties);

		FileExportTaskEntity runningTask = newTask(103L);
		FileExportTaskEntity cancelledTask = newTask(103L);
		cancelledTask.setStatus("CANCELLING");
		when(fileExportTaskMapper.selectClaimCandidateIds(any(), eq(5))).thenReturn(List.of(103L));
		when(fileExportTaskMapper.claimTask(eq(103L), eq("file-export-worker"), any(), any())).thenReturn(1);
		when(fileExportTaskMapper.selectById(103L)).thenReturn(runningTask, cancelledTask);

		worker.executeRound();

		verify(fileExportTaskMapper).markCancelled(eq(103L), eq("file-export-worker"), any());
		verify(asyncPagedExportExecutor, never()).export(any(), any(), any());
		verify(fileUploadService, never()).upload(any());

		ArgumentCaptor<FileExportAttemptEntity> captor = ArgumentCaptor.forClass(FileExportAttemptEntity.class);
		verify(fileExportAttemptMapper).insert(captor.capture());
		assertThat(captor.getValue().getResult()).isEqualTo("CANCELLED");
	}

	@Test
	@DisplayName("executeRound：attemptNo 由 retryCount+1 推导")
	void executeRoundUsesRetryCountPlusOneAsAttemptNo() {
		FileExportTaskProperties properties = newProperties();
		FileExportTaskWorker worker = newWorker(properties);

		FileExportTaskEntity task = newTask(105L);
		task.setRetryCount(2);
		when(fileExportTaskMapper.selectClaimCandidateIds(any(), eq(5))).thenReturn(List.of(105L));
		when(fileExportTaskMapper.claimTask(eq(105L), eq("file-export-worker"), any(), any())).thenReturn(1);
		when(fileExportTaskMapper.selectById(105L)).thenReturn(task);
		when(asyncPagedExportExecutor.export(any(FileExportTaskContext.class), any(), any()))
			.thenReturn(FileExportTaskArtifact.builder()
				.fileName("file_export.xlsx")
				.contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
				.filePath(writeArtifactFile())
				.rowCount(9)
				.build());
		FileUploadResultDTO uploadResult = new FileUploadResultDTO();
		uploadResult.setId(7002L);
		when(fileUploadService.upload(any())).thenReturn(uploadResult);

		worker.executeRound();

		ArgumentCaptor<FileExportAttemptEntity> captor = ArgumentCaptor.forClass(FileExportAttemptEntity.class);
		verify(fileExportAttemptMapper).insert(captor.capture());
		assertThat(captor.getValue().getAttemptNo()).isEqualTo(3);
		assertThat(captor.getValue().getResult()).isEqualTo("SUCCESS");
	}

	private FileExportTaskWorker newWorker(FileExportTaskProperties properties) {
		AuthProfileSecurityContextPopulator populator = new AuthProfileSecurityContextPopulator();
		FileExportTaskRunner runner = new FileExportTaskRunner(fileExportTaskMapper, fileUploadService,
				remotePageFetcher, asyncPagedExportExecutor, properties, populator);
		FileExportTaskFailureHandler failureHandler = new FileExportTaskFailureHandler(fileExportTaskMapper,
				properties);
		FileExportAttemptRecorder attemptRecorder = new FileExportAttemptRecorder(fileExportAttemptMapper);
		return new FileExportTaskWorker(fileExportTaskMapper, properties, runner, failureHandler, attemptRecorder);
	}

	private FileExportTaskProperties newProperties() {
		FileExportTaskProperties properties = new FileExportTaskProperties();
		properties.setClaimBatchSize(5);
		properties.setLeaseSeconds(60);
		properties.setArtifactExpireDays(7);
		properties.setRetryBaseDelaySeconds(30);
		return properties;
	}

	private FileExportTaskEntity newTask(Long id) {
		FileExportTaskEntity task = new FileExportTaskEntity();
		task.setId(id);
		task.setBizType("FILE_RECORD");
		task.setFetchService("service-system");
		task.setFetchPath("/api/system/inner/export/page");
		task.setQuerySnapshotJson("{}");
		task.setColumnSnapshotJson("[{\"fieldKey\":\"id\",\"header\":\"主键ID\"}]");
		task.setCreatedBy(9001L);
		task.setCreatedAt(Instant.parse("2026-08-07T00:00:00Z"));
		task.setRetryCount(0);
		task.setMaxRetry(3);
		task.setStatus("RUNNING");
		task.setScopeSnapshotJson(FileExportScopeSnapshots.toJson(AuthProfile.builder()
			.userId(9001L)
			.deptScope(
					ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT_AND_CHILD).values(List.of(110L)).build())
			.permVersion(1L)
			.build()));
		return task;
	}

	private Path writeArtifactFile() {
		try {
			Path file = Files.createTempFile("file-export-task-worker-test-", ".xlsx");
			Files.write(file, new byte[] { 1, 2, 3 });
			return file;
		}
		catch (Exception exception) {
			throw new IllegalStateException("Failed to build test artifact", exception);
		}
	}

}
