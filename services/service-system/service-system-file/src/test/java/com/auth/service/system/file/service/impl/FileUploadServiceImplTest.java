package com.auth.service.system.file.service.impl;

import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.config.FileUploadProperties;
import com.auth.service.system.file.exception.FileException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.SysFileRecordMapper;
import com.auth.service.system.file.model.entity.SysFileRecordEntity;
import com.auth.service.system.file.model.form.FileUploadForm;
import com.auth.service.system.file.model.value.FileUploadCommand;
import com.auth.service.system.file.model.value.StoredFile;
import com.auth.service.system.file.model.vo.FileUploadResponseVO;
import com.auth.service.system.file.storage.FileStorageProvider;
import com.auth.service.system.file.storage.FileStorageProviderRegistry;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link FileUploadServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileUploadServiceImpl 上传")
@ExtendWith(MockitoExtension.class)
class FileUploadServiceImplTest {

	@Mock
	private FileStorageProviderRegistry storageProviderRegistry;

	@Mock
	private FileStorageProvider fileStorageProvider;

	@Mock
	private SysFileRecordMapper sysFileRecordMapper;

	private FileUploadServiceImpl fileUploadService;

	@BeforeEach
	void setUp() throws Exception {
		FileUploadProperties fileUploadProperties = new FileUploadProperties();
		fileUploadProperties.setDefaultPlatform(StoragePlatformEnum.MINIO);
		fileUploadService = spy(new FileUploadServiceImpl(fileUploadProperties, storageProviderRegistry));
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(fileUploadService, sysFileRecordMapper);
	}

	@Test
	@DisplayName("upload：请求不指定平台时走默认平台并完成落库")
	void uploadUsesDefaultPlatformWhenRequestPlatformMissing() {
		// 验证默认平台路由、上传成功后落库与响应字段映射。
		FileUploadForm form = buildForm(new MockMultipartFile("file", "avatar.png", "image/png", "demo".getBytes()));
		StoredFile storedFile = buildStoredFile(StoragePlatformEnum.MINIO, "avatar/20260702/object.png");
		when(storageProviderRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(fileStorageProvider);
		when(fileStorageProvider.upload(any(FileUploadCommand.class))).thenReturn(storedFile);
		doAnswer(invocation -> {
			SysFileRecordEntity entity = invocation.getArgument(0);
			entity.setId(1001L);
			return true;
		}).when(fileUploadService).save(any(SysFileRecordEntity.class));

		FileUploadResponseVO response = fileUploadService.upload(form);

		ArgumentCaptor<SysFileRecordEntity> entityCaptor = ArgumentCaptor.forClass(SysFileRecordEntity.class);
		verify(fileUploadService).save(entityCaptor.capture());
		SysFileRecordEntity savedEntity = entityCaptor.getValue();
		assertThat(savedEntity.getStoragePlatform()).isEqualTo(StoragePlatformEnum.MINIO);
		assertThat(savedEntity.getUploadMode()).isEqualTo("SIMPLE");
		assertThat(savedEntity.getBizType()).isEqualTo("avatar");
		assertThat(response.getId()).isEqualTo(1001L);
		assertThat(response.getStoragePlatform()).isEqualTo(StoragePlatformEnum.MINIO);
	}

	@Test
	@DisplayName("upload：请求指定平台时优先使用请求平台")
	void uploadUsesRequestPlatformWhenSpecified() {
		// 验证请求参数 storagePlatform 优先于默认配置。
		FileUploadForm form = buildForm(new MockMultipartFile("file", "doc.pdf", "application/pdf", "demo".getBytes()));
		form.setStoragePlatform("aliyun_oss");
		StoredFile storedFile = buildStoredFile(StoragePlatformEnum.ALIYUN_OSS, "attachment/20260702/object.pdf");
		when(storageProviderRegistry.resolve(StoragePlatformEnum.ALIYUN_OSS)).thenReturn(fileStorageProvider);
		when(fileStorageProvider.upload(any(FileUploadCommand.class))).thenReturn(storedFile);
		doReturn(true).when(fileUploadService).save(any(SysFileRecordEntity.class));

		fileUploadService.upload(form);

		verify(storageProviderRegistry).resolve(StoragePlatformEnum.ALIYUN_OSS);
	}

	@Test
	@DisplayName("upload：bizType 路径片段会按规则标准化")
	void uploadNormalizesBizTypePathSegment() {
		// 验证业务类型中的特殊字符会被替换为下划线，避免对象键出现非法片段。
		FileUploadForm form = buildForm(new MockMultipartFile("file", "doc.txt", "text/plain", "demo".getBytes()));
		form.setBizType("avatar test@2026");
		StoredFile storedFile = buildStoredFile(StoragePlatformEnum.MINIO, "avatar_test_2026/20260702/object.txt");
		when(storageProviderRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(fileStorageProvider);
		when(fileStorageProvider.upload(any(FileUploadCommand.class))).thenReturn(storedFile);
		doReturn(true).when(fileUploadService).save(any(SysFileRecordEntity.class));

		fileUploadService.upload(form);

		ArgumentCaptor<FileUploadCommand> commandCaptor = ArgumentCaptor.forClass(FileUploadCommand.class);
		verify(fileStorageProvider).upload(commandCaptor.capture());
		assertThat(commandCaptor.getValue().getObjectKey()).startsWith("avatar_test_2026/");
	}

	@Test
	@DisplayName("upload：文件为空时抛出 PARAM_REQUIRED")
	void uploadThrowsWhenFileMissing() {
		// 验证空文件参数会在服务层被拦截。
		FileUploadForm form = new FileUploadForm();

		assertThatThrownBy(() -> fileUploadService.upload(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_REQUIRED);
	}

	@Test
	@DisplayName("upload：平台非法时抛出 FILE_STORAGE_PLATFORM_UNSUPPORTED")
	void uploadThrowsWhenPlatformInvalid() {
		// 验证非法平台字符串能够映射为统一业务异常码。
		FileUploadForm form = buildForm(new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "demo".getBytes()));
		form.setStoragePlatform("s3");

		assertThatThrownBy(() -> fileUploadService.upload(form)).isInstanceOf(FileException.class)
			.extracting(ex -> ((FileException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_STORAGE_PLATFORM_UNSUPPORTED);
	}

	private FileUploadForm buildForm(MockMultipartFile file) {
		FileUploadForm form = new FileUploadForm();
		form.setFile(file);
		form.setBizType("avatar");
		form.setBizId("u1001");
		form.setRemark("test");
		return form;
	}

	private StoredFile buildStoredFile(StoragePlatformEnum platform, String objectKey) {
		return StoredFile.builder()
			.storagePlatform(platform)
			.bucket("public")
			.objectKey(objectKey)
			.url("https://cdn.example.com/" + objectKey)
			.originalName("origin")
			.extension("png")
			.contentType("image/png")
			.size(1024L)
			.etag("etag-value")
			.build();
	}

}
