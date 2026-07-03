package com.auth.service.system.file.storage.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.FileUploadProperties;
import com.auth.service.system.file.exception.FileException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.model.value.FileUploadCommand;
import com.auth.service.system.file.model.value.StoredFile;
import com.auth.service.system.file.storage.FileStorageProvider;
import com.auth.service.system.file.utils.StorageUrlUtil;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * MinIO 文件上传实现
 *
 * @author Bunny
 */
@Component
public class MinioFileStorageProvider implements FileStorageProvider {

	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

	private final FileUploadProperties properties;

	public MinioFileStorageProvider(FileUploadProperties properties) {
		this.properties = properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StoragePlatformEnum platform() {
		return StoragePlatformEnum.MINIO;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StoredFile upload(FileUploadCommand command) {
		properties.assertMinioConfigured();
		FileUploadProperties.MinioConfig config = properties.getMinio();
		MinioClient minioClient = MinioClient.builder()
			.endpoint(config.getEndpoint())
			.credentials(config.getAccessKey(), config.getSecretKey())
			.build();

		String contentType = CharSequenceUtil.blankToDefault(command.getFile().getContentType(), DEFAULT_CONTENT_TYPE);
		try (InputStream inputStream = command.getFile().getInputStream()) {
			ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
				.bucket(config.getBucket())
				.object(command.getObjectKey())
				.stream(inputStream, command.getFile().getSize(), -1)
				.contentType(contentType)
				.build());

			return StoredFile.builder()
				.storagePlatform(platform())
				.bucket(config.getBucket())
				.objectKey(command.getObjectKey())
				.url(resolvePublicUrl(config, command.getObjectKey()))
				.originalName(command.getFile().getOriginalFilename())
				.extension(cn.hutool.core.io.FileUtil.extName(command.getFile().getOriginalFilename()))
				.contentType(contentType)
				.size(command.getFile().getSize())
				.etag(response.etag())
				.build();
		}
		catch (Exception exception) {
			throw new FileException(FileUploadResultCode.FILE_UPLOAD_FAILED, platform().name(), exception.getMessage());
		}
	}

	/**
	 * 解析 MinIO 对外访问地址
	 * @param config MinIO 配置
	 * @param objectKey 对象键
	 * @return 访问地址
	 */
	private String resolvePublicUrl(FileUploadProperties.MinioConfig config, String objectKey) {
		String objectPath = StorageUrlUtil.trimLeadingSlash(objectKey);
		if (CharSequenceUtil.isNotBlank(config.getPublicUrl())) {
			return StorageUrlUtil.concatPublicUrl(config.getPublicUrl(), objectPath);
		}
		String endpointBaseUrl = StorageUrlUtil.trimTrailingSlash(config.getEndpoint());
		return StorageUrlUtil.concatPublicUrl(endpointBaseUrl + "/" + config.getBucket(), objectPath);
	}

}
