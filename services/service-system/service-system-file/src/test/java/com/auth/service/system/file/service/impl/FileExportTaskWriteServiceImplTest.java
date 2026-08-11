package com.auth.service.system.file.service.impl;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.module.file.api.model.request.FileExportTaskCreateRequest;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.config.properties.FileExportTaskProperties;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.mapper.FileExportTaskMapper;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.model.enums.FileExportTaskStatus;
import com.auth.service.system.file.model.vo.FileExportTaskDownloadLinkVO;
import com.auth.service.system.file.support.FileUrlSigner;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link FileExportTaskWriteServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("FileExportTaskWriteServiceImpl 写服务")
@ExtendWith(MockitoExtension.class)
class FileExportTaskWriteServiceImplTest {

	@Mock
	private FileExportTaskMapper fileExportTaskMapper;

	@Mock
	private FileRecordMapper fileRecordMapper;

	@Mock
	private FileUrlSigner fileUrlSigner;

	@BeforeAll
	static void initMybatisPlusTableInfo() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
				FileExportTaskEntity.class);
	}

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("createTask：命中幂等请求返回已存在任务ID")
	void createTaskReturnsDuplicatedTaskByRequestId() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		FileExportTaskCreateRequest request = new FileExportTaskCreateRequest();
		request.setBizType("FILE_RECORD");
		request.setExportFormat("XLSX");
		request.setQuerySnapshotJson("{\"ownerUserId\":1}");
		request.setColumnSnapshotJson("[{\"fieldKey\":\"id\",\"header\":\"主键ID\"}]");
		request.setFetchService("service-system");
		request.setFetchPath("/api/system/inner/export/page");
		request.setRequestId("r-1");
		request.setCreatedBy(1L);
		AuthProfile profile = AuthProfile.builder().userId(1L).username("u1").build();
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken(profile, null, null));

		FileExportTaskEntity duplicatedResult = new FileExportTaskEntity();
		setBaseId(duplicatedResult);
		when(fileExportTaskMapper.selectOne(any())).thenReturn(duplicatedResult);
		Long taskId = service.createTask(request).getTaskId();
		assertThat(taskId).isEqualTo(9001L);
	}

	@Test
	@DisplayName("cancelTask：待处理任务优先直接取消")
	void cancelTaskCancelsPendingTask() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		when(fileExportTaskMapper.cancelPendingTask(eq(1001L), eq(2001L), any())).thenReturn(1);

		service.cancelTask(1001L, 2001L);

		verify(fileExportTaskMapper).cancelPendingTask(eq(1001L), eq(2001L), any());
		verify(fileExportTaskMapper, never()).requestCancelRunningTask(any(), any());
	}

	@Test
	@DisplayName("cancelTask：运行中任务标记为 CANCELLING")
	void cancelTaskRequestsCancelWhenRunning() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		when(fileExportTaskMapper.cancelPendingTask(eq(1010L), eq(2010L), any())).thenReturn(0);
		when(fileExportTaskMapper.requestCancelRunningTask(1010L, 2010L)).thenReturn(1);

		service.cancelTask(1010L, 2010L);

		verify(fileExportTaskMapper).requestCancelRunningTask(1010L, 2010L);
	}

	@Test
	@DisplayName("cancelTask：CANCELLING 状态幂等成功")
	void cancelTaskIsIdempotentWhenCancelling() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		FileExportTaskEntity task = new FileExportTaskEntity();
		task.setStatus(FileExportTaskStatus.CANCELLING.name());
		when(fileExportTaskMapper.cancelPendingTask(eq(1011L), eq(2011L), any())).thenReturn(0);
		when(fileExportTaskMapper.requestCancelRunningTask(1011L, 2011L)).thenReturn(0);
		when(fileExportTaskMapper.selectOne(any())).thenReturn(task);

		assertThatCode(() -> service.cancelTask(1011L, 2011L)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("cancelTask：CANCELLED 状态幂等成功")
	void cancelTaskIsIdempotentWhenCancelled() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		FileExportTaskEntity task = new FileExportTaskEntity();
		task.setStatus(FileExportTaskStatus.CANCELLED.name());
		when(fileExportTaskMapper.cancelPendingTask(eq(1012L), eq(2012L), any())).thenReturn(0);
		when(fileExportTaskMapper.requestCancelRunningTask(1012L, 2012L)).thenReturn(0);
		when(fileExportTaskMapper.selectOne(any())).thenReturn(task);

		assertThatCode(() -> service.cancelTask(1012L, 2012L)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("cancelTask：SUCCESS 状态拒绝取消")
	void cancelTaskThrowsWhenStatusNotCancellable() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		FileExportTaskEntity task = new FileExportTaskEntity();
		task.setStatus(FileExportTaskStatus.SUCCESS.name());
		when(fileExportTaskMapper.cancelPendingTask(eq(1013L), eq(2013L), any())).thenReturn(0);
		when(fileExportTaskMapper.requestCancelRunningTask(1013L, 2013L)).thenReturn(0);
		when(fileExportTaskMapper.selectOne(any())).thenReturn(task);

		assertThatThrownBy(() -> service.cancelTask(1013L, 2013L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_INVALID);
	}

	@Test
	@DisplayName("cancelTask：任务不存在时抛出 DATA_UNAVAILABLE")
	void cancelTaskThrowsWhenTaskMissing() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		when(fileExportTaskMapper.cancelPendingTask(eq(1003L), eq(2003L), any())).thenReturn(0);
		when(fileExportTaskMapper.requestCancelRunningTask(1003L, 2003L)).thenReturn(0);
		when(fileExportTaskMapper.selectOne(any())).thenReturn(null);

		assertThatThrownBy(() -> service.cancelTask(1003L, 2003L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);
	}

	@Test
	@DisplayName("retryTask：失败任务可重试")
	void retryTaskSucceedsWhenFailed() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		when(fileExportTaskMapper.retryTask(eq(1020L), eq(2020L), any())).thenReturn(1);

		service.retryTask(1020L, 2020L);

		verify(fileExportTaskMapper).retryTask(eq(1020L), eq(2020L), any());
	}

	@Test
	@DisplayName("retryTask：状态不允许重试时抛出 DATA_INVALID")
	void retryTaskThrowsWhenStatusNotRetryable() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		FileExportTaskEntity task = new FileExportTaskEntity();
		task.setStatus(FileExportTaskStatus.RUNNING.name());
		when(fileExportTaskMapper.retryTask(eq(1002L), eq(2002L), any())).thenReturn(0);
		when(fileExportTaskMapper.selectOne(any())).thenReturn(task);

		assertThatThrownBy(() -> service.retryTask(1002L, 2002L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_INVALID);
	}

	@Test
	@DisplayName("retryTask：CANCELLING 状态不可重试")
	void retryTaskThrowsWhenCancelling() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		FileExportTaskEntity task = new FileExportTaskEntity();
		task.setStatus(FileExportTaskStatus.CANCELLING.name());
		when(fileExportTaskMapper.retryTask(eq(1021L), eq(2021L), any())).thenReturn(0);
		when(fileExportTaskMapper.selectOne(any())).thenReturn(task);

		assertThatThrownBy(() -> service.retryTask(1021L, 2021L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_INVALID);
	}

	@Test
	@DisplayName("retryTask：SUCCESS 状态不可重试")
	void retryTaskThrowsWhenSuccess() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		FileExportTaskEntity task = new FileExportTaskEntity();
		task.setStatus(FileExportTaskStatus.SUCCESS.name());
		when(fileExportTaskMapper.retryTask(eq(1022L), eq(2022L), any())).thenReturn(0);
		when(fileExportTaskMapper.selectOne(any())).thenReturn(task);

		assertThatThrownBy(() -> service.retryTask(1022L, 2022L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_INVALID);
	}

	@Test
	@DisplayName("retryTask：EXPIRED 状态不可重试")
	void retryTaskThrowsWhenExpired() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		FileExportTaskEntity task = new FileExportTaskEntity();
		task.setStatus(FileExportTaskStatus.EXPIRED.name());
		when(fileExportTaskMapper.retryTask(eq(1023L), eq(2023L), any())).thenReturn(0);
		when(fileExportTaskMapper.selectOne(any())).thenReturn(task);

		assertThatThrownBy(() -> service.retryTask(1023L, 2023L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_INVALID);
	}

	@Test
	@DisplayName("retryTask：任务不存在时抛出 DATA_UNAVAILABLE")
	void retryTaskThrowsWhenTaskMissing() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		when(fileExportTaskMapper.retryTask(eq(1004L), eq(2004L), any())).thenReturn(0);
		when(fileExportTaskMapper.selectOne(any())).thenReturn(null);

		assertThatThrownBy(() -> service.retryTask(1004L, 2004L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);
	}

	@Test
	@DisplayName("getDownloadLink：成功任务返回签名下载地址")
	void getDownloadLinkReturnsSignedUrl() {
		FileUploadProperties uploadProperties = new FileUploadProperties();
		uploadProperties.setExpireSeconds(600);
		FileExportTaskWriteServiceImpl service = newService(uploadProperties, new FileExportTaskProperties());

		FileExportTaskEntity task = new FileExportTaskEntity();
		task.setStatus(FileExportTaskStatus.SUCCESS.name());
		task.setFileRecordId(7001L);
		task.setExpireAt(Instant.now().plusSeconds(600));
		when(fileExportTaskMapper.selectOne(any())).thenReturn(task);

		FileRecordEntity fileRecord = new FileRecordEntity();
		fileRecord.setOriginalName("file_record_export.xlsx");
		fileRecord.setBucket("private");
		fileRecord.setObjectKey("x/y/z.xlsx");
		fileRecord.setStoragePlatform(StoragePlatformEnum.MINIO);
		when(fileRecordMapper.selectDetailById(7001L, null, null, false, null)).thenReturn(fileRecord);
		when(fileUrlSigner.signDownload("private", "x/y/z.xlsx", StoragePlatformEnum.MINIO))
			.thenReturn("https://minio.local/download");

		FileExportTaskDownloadLinkVO link = service.getDownloadLink(3001L, 4001L);

		assertThat(link.getUrl()).isEqualTo("https://minio.local/download");
		assertThat(link.getFileName()).isEqualTo("file_record_export.xlsx");
		assertThat(link.getExpireSeconds()).isEqualTo(600);
	}

	@Test
	@DisplayName("getDownloadLink：非 SUCCESS 状态拒绝下载")
	void getDownloadLinkThrowsWhenStatusNotSuccess() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		FileExportTaskEntity task = new FileExportTaskEntity();
		task.setStatus(FileExportTaskStatus.FAILED.name());
		when(fileExportTaskMapper.selectOne(any())).thenReturn(task);

		assertThatThrownBy(() -> service.getDownloadLink(3002L, 4002L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_INVALID);
	}

	@Test
	@DisplayName("getDownloadLink：产物已过期拒绝下载")
	void getDownloadLinkThrowsWhenArtifactExpired() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		FileExportTaskEntity task = new FileExportTaskEntity();
		task.setStatus(FileExportTaskStatus.SUCCESS.name());
		task.setFileRecordId(7002L);
		task.setExpireAt(Instant.now().minusSeconds(60));
		when(fileExportTaskMapper.selectOne(any())).thenReturn(task);

		assertThatThrownBy(() -> service.getDownloadLink(3003L, 4003L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);
	}

	@Test
	@DisplayName("forceCancelTask：不校验创建人即可取消待处理任务")
	void forceCancelTaskCancelsPendingWithoutOwnerCheck() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		when(fileExportTaskMapper.cancelPendingTask(eq(1101L), isNull(), any())).thenReturn(1);

		service.forceCancelTask(1101L);

		verify(fileExportTaskMapper).cancelPendingTask(eq(1101L), isNull(), any());
		verify(fileExportTaskMapper, never()).requestCancelRunningTask(any(), any());
	}

	@Test
	@DisplayName("forceCancelTask：运行中任务标记为 CANCELLING")
	void forceCancelTaskRequestsCancelWhenRunning() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		when(fileExportTaskMapper.cancelPendingTask(eq(1102L), isNull(), any())).thenReturn(0);
		when(fileExportTaskMapper.requestCancelRunningTask(eq(1102L), isNull())).thenReturn(1);

		service.forceCancelTask(1102L);

		verify(fileExportTaskMapper).requestCancelRunningTask(eq(1102L), isNull());
	}

	@Test
	@DisplayName("forceCancelTask：CANCELLING 状态幂等成功")
	void forceCancelTaskIsIdempotentWhenCancelling() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		FileExportTaskEntity task = new FileExportTaskEntity();
		task.setStatus(FileExportTaskStatus.CANCELLING.name());
		when(fileExportTaskMapper.cancelPendingTask(eq(1103L), isNull(), any())).thenReturn(0);
		when(fileExportTaskMapper.requestCancelRunningTask(eq(1103L), isNull())).thenReturn(0);
		when(fileExportTaskMapper.selectById(1103L)).thenReturn(task);

		assertThatCode(() -> service.forceCancelTask(1103L)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("forceCancelTask：SUCCESS 状态拒绝取消")
	void forceCancelTaskThrowsWhenStatusNotCancellable() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		FileExportTaskEntity task = new FileExportTaskEntity();
		task.setStatus(FileExportTaskStatus.SUCCESS.name());
		when(fileExportTaskMapper.cancelPendingTask(eq(1104L), isNull(), any())).thenReturn(0);
		when(fileExportTaskMapper.requestCancelRunningTask(eq(1104L), isNull())).thenReturn(0);
		when(fileExportTaskMapper.selectById(1104L)).thenReturn(task);

		assertThatThrownBy(() -> service.forceCancelTask(1104L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_INVALID);
	}

	@Test
	@DisplayName("forceCancelTask：任务不存在时抛出 DATA_UNAVAILABLE")
	void forceCancelTaskThrowsWhenTaskMissing() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		when(fileExportTaskMapper.cancelPendingTask(eq(1105L), isNull(), any())).thenReturn(0);
		when(fileExportTaskMapper.requestCancelRunningTask(eq(1105L), isNull())).thenReturn(0);
		when(fileExportTaskMapper.selectById(1105L)).thenReturn(null);

		assertThatThrownBy(() -> service.forceCancelTask(1105L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);
	}

	@Test
	@DisplayName("forceRetryTask：不校验创建人即可重试失败任务")
	void forceRetryTaskSucceedsWithoutOwnerCheck() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		when(fileExportTaskMapper.forceRetryTask(eq(1201L), isNull(), any())).thenReturn(1);

		service.forceRetryTask(1201L);

		verify(fileExportTaskMapper).forceRetryTask(eq(1201L), isNull(), any());
		verify(fileExportTaskMapper, never()).retryTask(any(), any(), any());
	}

	@Test
	@DisplayName("forceRetryTask：已达 max_retry 仍可强制重试")
	void forceRetryTaskSucceedsWhenRetryExhausted() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		when(fileExportTaskMapper.forceRetryTask(eq(1204L), isNull(), any())).thenReturn(1);

		service.forceRetryTask(1204L);

		verify(fileExportTaskMapper).forceRetryTask(eq(1204L), isNull(), any());
	}

	@Test
	@DisplayName("forceRetryTask：状态不允许重试时抛出 DATA_INVALID")
	void forceRetryTaskThrowsWhenStatusNotRetryable() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		FileExportTaskEntity task = new FileExportTaskEntity();
		task.setStatus(FileExportTaskStatus.RUNNING.name());
		when(fileExportTaskMapper.forceRetryTask(eq(1202L), isNull(), any())).thenReturn(0);
		when(fileExportTaskMapper.selectById(1202L)).thenReturn(task);

		assertThatThrownBy(() -> service.forceRetryTask(1202L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_INVALID);
	}

	@Test
	@DisplayName("forceRetryTask：任务不存在时抛出 DATA_UNAVAILABLE")
	void forceRetryTaskThrowsWhenTaskMissing() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		when(fileExportTaskMapper.forceRetryTask(eq(1203L), isNull(), any())).thenReturn(0);
		when(fileExportTaskMapper.selectById(1203L)).thenReturn(null);

		assertThatThrownBy(() -> service.forceRetryTask(1203L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);
	}

	@Test
	@DisplayName("retryTask：已达 max_retry 时抛出 DATA_INVALID(retryLimit)")
	void retryTaskThrowsWhenRetryExhausted() {
		FileExportTaskWriteServiceImpl service = newService(new FileUploadProperties(), new FileExportTaskProperties());

		FileExportTaskEntity task = new FileExportTaskEntity();
		task.setStatus(FileExportTaskStatus.FAILED.name());
		task.setRetryCount(3);
		task.setMaxRetry(3);
		when(fileExportTaskMapper.retryTask(eq(1024L), eq(2024L), any())).thenReturn(0);
		when(fileExportTaskMapper.selectOne(any())).thenReturn(task);

		assertThatThrownBy(() -> service.retryTask(1024L, 2024L)).isInstanceOf(SystemBusinessException.class)
			.satisfies(ex -> {
				SystemBusinessException businessException = (SystemBusinessException) ex;
				assertThat(businessException.getResultCode()).isEqualTo(SystemCommonResultCode.DATA_INVALID);
				assertThat(businessException.getMessageArgs()).containsExactly("retryLimit");
			});
	}

	private FileExportTaskWriteServiceImpl newService(FileUploadProperties uploadProperties,
			FileExportTaskProperties taskProperties) {
		FileExportTaskWriteServiceImpl service = new FileExportTaskWriteServiceImpl(fileRecordMapper, fileUrlSigner,
				uploadProperties, taskProperties);
		ReflectionTestUtils.setField(service, "baseMapper", fileExportTaskMapper);
		ReflectionTestUtils.setField(service, "entityClass", FileExportTaskEntity.class);
		return service;
	}

	private void setBaseId(FileExportTaskEntity entity) {
		try {
			Field idField = entity.getClass().getSuperclass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(entity, (Long) 9001L);
		}
		catch (ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}

}
