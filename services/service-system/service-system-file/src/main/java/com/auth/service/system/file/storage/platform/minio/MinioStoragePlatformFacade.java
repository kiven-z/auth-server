package com.auth.service.system.file.storage.platform.minio;

import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.file.storage.core.StoragePlatformFacade;
import com.auth.service.system.file.storage.core.bucket.StorageBucketInitializer;
import com.auth.service.system.file.storage.core.classifier.StorageExceptionClassifier;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import com.auth.service.system.file.storage.core.s3.S3CompatibleFileStorageProvider;
import com.auth.service.system.file.storage.core.validator.StoragePlatformConfigValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * MinIO 平台门面
 *
 * @author Bunny
 */
@Component
public class MinioStoragePlatformFacade implements StoragePlatformFacade {

	private final S3CompatibleFileStorageProvider fileStorageProvider;

	private final MinioConfigValidator configValidator;

	private final MinioBucketInitializer bucketInitializer;

	private final StorageExceptionClassifier exceptionClassifier;

	public MinioStoragePlatformFacade(
			@Qualifier("minioFileStorageProvider") S3CompatibleFileStorageProvider fileStorageProvider,
			MinioConfigValidator configValidator, MinioBucketInitializer bucketInitializer,
			StorageExceptionClassifier exceptionClassifier) {
		this.fileStorageProvider = fileStorageProvider;
		this.configValidator = configValidator;
		this.bucketInitializer = bucketInitializer;
		this.exceptionClassifier = exceptionClassifier;
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
	public FileStorageProvider provider() {
		return fileStorageProvider;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StorageBucketInitializer initializer() {
		return bucketInitializer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StoragePlatformConfigValidator validator() {
		return configValidator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StorageExceptionClassifier exceptionClassifier() {
		return exceptionClassifier;
	}

}
