package com.auth.service.system.file.storage.platform.aliyun;

import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.S3PlatformProfile;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.storage.core.bucket.StorageBucketInitializer;
import com.auth.service.system.file.storage.core.classifier.StorageExceptionClassifier;
import com.auth.service.system.file.storage.core.s3.S3ClientManager;
import com.auth.service.system.file.storage.core.s3.S3PlatformProfileResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

/**
 * 阿里云 OSS 存储桶初始化器
 *
 * <p>
 * 走 S3 兼容协议：{@code headBucket} 探测存在性，缺失时 {@code createBucket}。 OSS 默认不需要额外设置
 * ACL；如后续需要私有/公开切换，可在此扩展。
 * </p>
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class AliyunOssBucketInitializer implements StorageBucketInitializer {

	private static final StoragePlatformEnum STORAGE_PLATFORM = StoragePlatformEnum.ALIYUN_OSS;

	private final S3PlatformProfileResolver profileResolver;

	private final S3ClientManager s3ClientManager;

	private final StorageExceptionClassifier exceptionClassifier;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void ensureBucketReady() {
		S3PlatformProfile profile = profileResolver.resolve(STORAGE_PLATFORM);
		String bucket = profile.getBucket();
		S3Client client = s3ClientManager.getClient(STORAGE_PLATFORM);
		try {
			if (bucketExists(client, bucket)) {
				log.info("OSS bucket already exists: bucket={}", bucket);
				return;
			}
			client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
			log.info("Created OSS bucket: bucket={}", bucket);
		}
		catch (Exception exception) {
			throw new FileStorageException(FileUploadResultCode.FILE_UPLOAD_FAILED, exception, STORAGE_PLATFORM.name(),
					exception.getMessage());
		}
	}

	private boolean bucketExists(S3Client client, String bucket) {
		try {
			client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
			return true;
		}
		catch (Exception exception) {
			if (exceptionClassifier.isNotFound(exception)) {
				return false;
			}
			throw exception;
		}
	}

}
