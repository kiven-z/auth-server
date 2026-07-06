package com.auth.service.system.file.storage.platform.minio;

import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.config.properties.platform.MinioStorageProperties;
import com.auth.service.system.file.storage.core.validator.StoragePlatformConfigValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * MinIO 平台配置校验器
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class MinioConfigValidator implements StoragePlatformConfigValidator {

	private final FileUploadProperties properties;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validateOrThrow() {
		MinioStorageProperties minio = Objects.requireNonNullElseGet(properties.getMinio(),
				MinioStorageProperties::new);
		assertNotBlank(minio.getEndpoint(), "minio.endpoint");
		assertNotBlank(minio.getAccessKey(), "minio.access-key");
		assertNotBlank(minio.getSecretKey(), "minio.secret-key");
		assertNotBlank(minio.getBucket(), "minio.bucket");
	}

}
