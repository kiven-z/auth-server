package com.auth.service.system.file.storage.core.provider;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.S3PlatformProfile;
import com.auth.service.system.file.model.value.FileUploadCommand;
import com.auth.service.system.file.model.value.StoredFile;
import com.auth.service.system.file.storage.core.classifier.StorageExceptionClassifier;
import com.auth.service.system.file.storage.core.s3.S3PlatformProfileResolver;
import com.auth.service.system.file.storage.core.validator.StoragePlatformConfigValidator;
import com.auth.service.system.file.utils.StorageUrlUtil;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;

/**
 * 基于 AWS SDK v2 的 S3 兼容存储 Provider
 *
 * <p>
 * 一份实现覆盖所有 S3 协议兼容平台（MinIO / 阿里云 OSS / 未来 AWS S3 / 腾讯 COS / 华为 OBS / 七牛 / Cloudflare R2）。
 * 由 {@link StoragePlatformEnum} 与 {@link S3PlatformProfile} 决定实际连接哪个端点。
 * </p>
 *
 * @author Bunny
 */
@RequiredArgsConstructor
public class S3CompatibleFileStorageProvider extends AbstractFileStorageProvider<S3Client> {

	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

	private final StoragePlatformEnum platform;

	private final S3PlatformProfileResolver profileResolver;

	private final S3ClientManager s3ClientManager;

	private final StoragePlatformConfigValidator configValidator;

	private final StorageExceptionClassifier storageExceptionClassifier;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected StoragePlatformEnum platform() {
		return platform;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected S3Client client() {
		return s3ClientManager.getClient(platform);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected StoragePlatformConfigValidator validator() {
		return configValidator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected StorageExceptionClassifier exceptionClassifier() {
		return storageExceptionClassifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected StoredFile doUpload(S3Client client, FileUploadCommand command) {
		S3PlatformProfile profile = profileResolver.resolve(platform);
		String contentType = CharSequenceUtil.blankToDefault(command.getFile().getContentType(), DEFAULT_CONTENT_TYPE);
		String bucket = profile.getBucket();
		String objectKey = command.getObjectKey();
		long size = command.getFile().getSize();

		try (InputStream inputStream = command.getFile().getInputStream()) {
			PutObjectRequest putRequest = PutObjectRequest.builder()
				.bucket(bucket)
				.key(objectKey)
				.contentType(contentType)
				.contentLength(size)
				.build();
			PutObjectResponse response = client.putObject(putRequest, RequestBody.fromInputStream(inputStream, size));

			return StoredFile.builder()
				.storagePlatform(platform)
				.bucket(bucket)
				.objectKey(objectKey)
				.url(resolvePublicUrl(bucket, objectKey))
				.originalName(command.getFile().getOriginalFilename())
				.extension(FileUtil.extName(command.getFile().getOriginalFilename()))
				.contentType(contentType)
				.size(size)
				.etag(response.eTag())
				.build();
		}
		catch (IOException ioException) {
			throw new UncheckedIOException(ioException);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected InputStream doDownload(S3Client client, String bucket, String objectKey) {
		return client.getObject(GetObjectRequest.builder().bucket(bucket).key(objectKey).build());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String doPresignGetUrl(S3Client client, String bucket, String objectKey, int expireSeconds) {
		S3Presigner presigner = s3ClientManager.getPresigner(platform);
		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
			.signatureDuration(Duration.ofSeconds(expireSeconds))
			.getObjectRequest(request -> request.bucket(bucket).key(objectKey))
			.build();
		PresignedGetObjectRequest presigned = presigner.presignGetObject(presignRequest);
		return presigned.url().toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doDelete(S3Client client, String bucket, String objectKey) {
		client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(objectKey).build());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doMove(S3Client client, String bucket, String sourceObjectKey, String destinationObjectKey) {
		boolean sourceExists = objectExists(bucket, sourceObjectKey);
		boolean destinationExists = objectExists(bucket, destinationObjectKey);

		if (!sourceExists) {
			if (destinationExists) {
				return;
			}
			throw S3Exception.builder()
				.statusCode(404)
				.awsErrorDetails(AwsErrorDetails.builder().errorCode("NoSuchKey").build())
				.message("Source object not found: " + sourceObjectKey)
				.build();
		}
		if (destinationExists) {
			throw S3Exception.builder()
				.statusCode(409)
				.awsErrorDetails(AwsErrorDetails.builder().errorCode("ObjectAlreadyExists").build())
				.message("Destination object already exists: " + destinationObjectKey)
				.build();
		}

		client.copyObject(CopyObjectRequest.builder()
			.sourceBucket(bucket)
			.sourceKey(sourceObjectKey)
			.destinationBucket(bucket)
			.destinationKey(destinationObjectKey)
			.build());

		try {
			client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(sourceObjectKey).build());
		}
		catch (S3Exception exception) {
			if (storageExceptionClassifier.isNotFound(exception)) {
				return;
			}
			throw exception;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean doObjectExists(S3Client client, String bucket, String objectKey) {
		try {
			client.headObject(HeadObjectRequest.builder().bucket(bucket).key(objectKey).build());
			return true;
		}
		catch (NoSuchKeyException exception) {
			return false;
		}
		catch (S3Exception exception) {
			if (storageExceptionClassifier.isNotFound(exception)) {
				return false;
			}
			throw exception;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String doResolvePublicUrl(String bucket, String objectKey) {
		S3PlatformProfile profile = profileResolver.resolve(platform);
		String objectPath = StorageUrlUtil.trimLeadingSlash(objectKey);
		if (CharSequenceUtil.isNotBlank(profile.getPublicUrl())) {
			return StorageUrlUtil.concatPublicUrl(profile.getPublicUrl(), objectPath);
		}
		String endpointBaseUrl = StorageUrlUtil.normalizeHttpBaseUrl(profile.getEndpoint());
		String resolvedBucket = CharSequenceUtil.blankToDefault(bucket, profile.getBucket());
		return StorageUrlUtil.concatPublicUrl(endpointBaseUrl + "/" + resolvedBucket, objectPath);
	}

}
