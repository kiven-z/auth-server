package com.auth.service.system.file.storage.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.FileUploadProperties;
import com.auth.service.system.file.exception.FileException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.model.value.FileUploadCommand;
import com.auth.service.system.file.model.value.StoredFile;
import com.auth.service.system.file.storage.FileStorageProvider;
import com.auth.service.system.file.utils.StorageDeleteSupport;
import com.auth.service.system.file.utils.StorageUrlUtil;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * MinIO 文件上传实现
 *
 * @author Bunny
 */
@Slf4j
@Component
public class MinioFileStorageProvider implements FileStorageProvider {

	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

	private final FileUploadProperties properties;

	private final AtomicReference<MinioClient> minioClientRef = new AtomicReference<>();

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
		String contentType = CharSequenceUtil.blankToDefault(command.getFile().getContentType(), DEFAULT_CONTENT_TYPE);
		try (InputStream inputStream = command.getFile().getInputStream()) {
			ObjectWriteResponse response = getMinioClient().putObject(PutObjectArgs.builder()
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
	 * {@inheritDoc}
	 */
	@Override
	public InputStream download(String bucket, String objectKey) {
		try {
			return getMinioClient().getObject(GetObjectArgs.builder().bucket(bucket).object(objectKey).build());
		}
		catch (Exception exception) {
			throw new FileException(FileUploadResultCode.FILE_UPLOAD_FAILED, platform().name(), exception.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String presignGetUrl(String bucket, String objectKey, int expireSeconds) {
		try {
			return getMinioClient().getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
				.method(Method.GET)
				.bucket(bucket)
				.object(objectKey)
				.expiry(expireSeconds, TimeUnit.SECONDS)
				.build());
		}
		catch (Exception exception) {
			throw new FileException(FileUploadResultCode.FILE_UPLOAD_FAILED, platform().name(), exception.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(String bucket, String objectKey) {
		try {
			getMinioClient().removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
		}
		catch (Exception exception) {
			if (StorageDeleteSupport.isMinioObjectNotFound(exception)) {
				log.warn("MinIO object already absent, skip delete: bucket={}, objectKey={}, reason={}", bucket,
						objectKey, exception.getMessage());
				return;
			}
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

	/**
	 * 获取 MinIO 客户端（懒加载单例）
	 * @return MinIO 客户端
	 */
	private MinioClient getMinioClient() {
		MinioClient localClient = minioClientRef.get();
		if (localClient != null) {
			return localClient;
		}
		synchronized (this) {
			localClient = minioClientRef.get();
			if (localClient == null) {
				properties.assertMinioConfigured();
				FileUploadProperties.MinioConfig config = properties.getMinio();
				localClient = MinioClient.builder()
					.endpoint(config.getEndpoint())
					.credentials(config.getAccessKey(), config.getSecretKey())
					.build();
				minioClientRef.set(localClient);
			}
			return localClient;
		}
	}

	/**
	 * Bean 销毁时关闭 MinIO 客户端
	 */
	@PreDestroy
	public void destroy() throws Exception {
		MinioClient localClient = minioClientRef.getAndSet(null);
		if (localClient != null) {
			localClient.close();
		}
	}

}
