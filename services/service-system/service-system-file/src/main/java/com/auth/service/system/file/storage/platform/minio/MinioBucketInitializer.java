package com.auth.service.system.file.storage.platform.minio;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.text.CharSequenceUtil;
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
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;

/**
 * MinIO 存储桶初始化器
 *
 * <p>
 * 走 S3 兼容协议：{@code headBucket} 探测存在性，缺失时 {@code createBucket}，再套用公开前缀策略。
 * </p>
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class MinioBucketInitializer implements StorageBucketInitializer {

	/**
	 * MinIO public 前缀策略模板路径
	 */
	public static final String POLICY_TEMPLATE_PATH = "storage/policies/minio-public-prefix-policy.json";

	/**
	 * 存储桶占位符
	 */
	public static final String BUCKET_PLACEHOLDER = "${bucket}";

	private static final StoragePlatformEnum STORAGE_PLATFORM = StoragePlatformEnum.MINIO;

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
				log.info("MinIO bucket already exists, keep current policy: bucket={}", bucket);
				return;
			}
			client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
			log.info("Created MinIO bucket: bucket={}", bucket);

			String template = ResourceUtil.readUtf8Str(POLICY_TEMPLATE_PATH);
			String policyJson = CharSequenceUtil.replace(template, BUCKET_PLACEHOLDER, bucket);
			client.putBucketPolicy(PutBucketPolicyRequest.builder().bucket(bucket).policy(policyJson).build());
			log.info("Applied MinIO public prefix policy after bucket creation: bucket={}", bucket);
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
