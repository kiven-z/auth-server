package com.auth.service.system.file.storage.platform.aliyun;

import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.file.storage.StoragePlatformFacade;
import com.auth.service.system.file.storage.core.bucket.StorageBucketInitializer;
import com.auth.service.system.file.storage.core.capability.ImageStyleUrlCapability;
import com.auth.service.system.file.storage.core.capability.StoragePlatformCapability;
import com.auth.service.system.file.storage.core.classifier.StorageExceptionClassifier;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import com.auth.service.system.file.storage.core.provider.S3CompatibleFileStorageProvider;
import com.auth.service.system.file.storage.core.validator.StoragePlatformConfigValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 阿里云 OSS 平台门面
 *
 * @author Bunny
 */
@Component
public class AliyunOssStoragePlatformFacade implements StoragePlatformFacade {

	private final S3CompatibleFileStorageProvider fileStorageProvider;

	private final AliyunOssConfigValidator configValidator;

	private final AliyunOssBucketInitializer bucketInitializer;

	private final StorageExceptionClassifier exceptionClassifier;

	private final AliyunImageStyleUrlCapability imageStyleUrlCapability;

	public AliyunOssStoragePlatformFacade(
			@Qualifier("aliyunOssFileStorageProvider") S3CompatibleFileStorageProvider fileStorageProvider,
			AliyunOssConfigValidator configValidator, AliyunOssBucketInitializer bucketInitializer,
			StorageExceptionClassifier exceptionClassifier, AliyunImageStyleUrlCapability imageStyleUrlCapability) {
		this.fileStorageProvider = fileStorageProvider;
		this.configValidator = configValidator;
		this.bucketInitializer = bucketInitializer;
		this.exceptionClassifier = exceptionClassifier;
		this.imageStyleUrlCapability = imageStyleUrlCapability;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StoragePlatformEnum platform() {
		return StoragePlatformEnum.ALIYUN_OSS;
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends StoragePlatformCapability> Optional<T> capability(Class<T> capabilityType) {
		if (capabilityType == ImageStyleUrlCapability.class) {
			return Optional.of(capabilityType.cast(imageStyleUrlCapability));
		}
		return Optional.empty();
	}

}
