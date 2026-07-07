package com.auth.service.system.file.service.impl;

import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.model.form.FileUploadForm;
import com.auth.service.system.file.model.value.FileUploadCommand;
import com.auth.service.system.file.model.value.StoredFile;
import com.auth.service.system.file.model.vo.FileUploadResponseVO;
import com.auth.service.system.file.storage.core.StoragePlatformFacade;
import com.auth.service.system.file.storage.core.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
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
	private StoragePlatformFacadeRegistry facadeRegistry;

	@Mock
	private StoragePlatformFacade storagePlatformFacade;

	@Mock
	private FileStorageProvider fileStorageProvider;

	@Mock
	private FileRecordMapper fileRecordMapper;

	private FileUploadServiceImpl fileUploadService;

	@BeforeEach
	void setUp() throws Exception {
		FileUploadProperties fileUploadProperties = new FileUploadProperties();
		fileUploadProperties.setDefaultPlatform(StoragePlatformEnum.MINIO);
		fileUploadService = spy(new FileUploadServiceImpl(fileUploadProperties, facadeRegistry));
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(fileUploadService, fileRecordMapper);
	}

	@Test
	@DisplayName("upload：avatar 固定公开前缀并落库 isPrivate=false")
	void uploadUsesPublicPrefixAndPersistsResolvedVisibilityForAvatar() {
		// 验证 avatar 业务类型固定公开，且对象键以 public/avatar 开头。
		FileUploadForm form = buildForm(new MockMultipartFile("file", "avatar.png", "image/png", "demo".getBytes()));
		form.setIsPrivate(true);
		StoredFile storedFile = buildStoredFile(StoragePlatformEnum.MINIO, "public/avatar/20260702/object.png");
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(fileStorageProvider);
		when(fileStorageProvider.upload(any(FileUploadCommand.class))).thenReturn(storedFile);
		doAnswer(invocation -> {
			FileRecordEntity entity = invocation.getArgument(0);
			entity.setId(1001L);
			return true;
		}).when(fileUploadService).save(any(FileRecordEntity.class));

		FileUploadResponseVO response = fileUploadService.upload(form);

		ArgumentCaptor<FileRecordEntity> entityCaptor = ArgumentCaptor.forClass(FileRecordEntity.class);
		verify(fileUploadService).save(entityCaptor.capture());
		FileRecordEntity savedEntity = entityCaptor.getValue();
		assertThat(savedEntity.getStoragePlatform()).isEqualTo(StoragePlatformEnum.MINIO);
		assertThat(savedEntity.getUploadMode()).isEqualTo("SIMPLE");
		assertThat(savedEntity.getBizType()).isEqualTo("avatar");
		assertThat(savedEntity.getIsPrivate()).isFalse();
		ArgumentCaptor<FileUploadCommand> commandCaptor = ArgumentCaptor.forClass(FileUploadCommand.class);
		verify(fileStorageProvider).upload(commandCaptor.capture());
		assertThat(commandCaptor.getValue().getObjectKey()).startsWith("public/avatar/");
		assertThat(response.getId()).isEqualTo(1001L);
		assertThat(response.getStoragePlatform()).isEqualTo(StoragePlatformEnum.MINIO);
	}

	@Test
	@DisplayName("upload：attachment 默认私有并写入 private 前缀")
	void uploadPersistsPrivateFlagForAttachmentBizType() {
		// 验证 attachment 策略默认私有，且对象键以 private/attachment 开头。
		FileUploadForm form = buildForm(new MockMultipartFile("file", "private.txt", "text/plain", "demo".getBytes()));
		form.setBizType("attachment");
		StoredFile storedFile = buildStoredFile(StoragePlatformEnum.MINIO, "private/attachment/20260702/object.txt");
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(fileStorageProvider);
		when(fileStorageProvider.upload(any(FileUploadCommand.class))).thenReturn(storedFile);
		doReturn(true).when(fileUploadService).save(any(FileRecordEntity.class));

		fileUploadService.upload(form);

		ArgumentCaptor<FileRecordEntity> entityCaptor = ArgumentCaptor.forClass(FileRecordEntity.class);
		verify(fileUploadService).save(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getIsPrivate()).isTrue();
		ArgumentCaptor<FileUploadCommand> commandCaptor = ArgumentCaptor.forClass(FileUploadCommand.class);
		verify(fileStorageProvider).upload(commandCaptor.capture());
		assertThat(commandCaptor.getValue().getObjectKey()).startsWith("private/attachment/");
	}

	@Test
	@DisplayName("upload：未知 bizType 时抛出 IllegalArgumentException")
	void uploadThrowsWhenBizTypeInvalid() {
		// 验证未知业务类型在严格模式下被拒绝。
		FileUploadForm form = buildForm(new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "demo".getBytes()));
		form.setBizType("foo");

		assertThatThrownBy(() -> fileUploadService.upload(form)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("foo");
	}

	@Test
	@DisplayName("upload：请求指定平台时优先使用请求平台")
	void uploadUsesRequestPlatformWhenSpecified() {
		// 验证请求参数 storagePlatform 优先于默认配置。
		FileUploadForm form = buildForm(new MockMultipartFile("file", "doc.pdf", "application/pdf", "demo".getBytes()));
		form.setStoragePlatform("aliyun_oss");
		StoredFile storedFile = buildStoredFile(StoragePlatformEnum.ALIYUN_OSS, "attachment/20260702/object.pdf");
		when(facadeRegistry.resolve(StoragePlatformEnum.ALIYUN_OSS)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(fileStorageProvider);
		when(fileStorageProvider.upload(any(FileUploadCommand.class))).thenReturn(storedFile);
		doReturn(true).when(fileUploadService).save(any(FileRecordEntity.class));

		fileUploadService.upload(form);

		verify(facadeRegistry).resolve(StoragePlatformEnum.ALIYUN_OSS);
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
	@DisplayName("upload：平台非法时抛出 IllegalArgumentException")
	void uploadThrowsWhenPlatformInvalid() {
		// 验证非法平台字符串会被枚举严格解析直接拒绝。
		FileUploadForm form = buildForm(new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "demo".getBytes()));
		form.setStoragePlatform("s3");

		assertThatThrownBy(() -> fileUploadService.upload(form)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Unsupported storage platform");
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
