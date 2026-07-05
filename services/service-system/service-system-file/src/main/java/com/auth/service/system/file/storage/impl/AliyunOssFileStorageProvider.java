package com.auth.service.system.file.storage.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.FileUploadProperties;
import com.auth.service.system.file.exception.FileException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.model.value.FileUploadCommand;
import com.auth.service.system.file.model.value.StoredFile;
import com.auth.service.system.file.storage.FileStorageProvider;
import com.auth.service.system.file.utils.StorageDeleteSupport;
import com.auth.service.system.file.utils.StorageUrlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Date;

/**
 * 阿里云 OSS 文件上传实现
 *
 * @author Bunny
 */
@Slf4j
@Component
public class AliyunOssFileStorageProvider implements FileStorageProvider {

	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

	private final FileUploadProperties properties;

	public AliyunOssFileStorageProvider(FileUploadProperties properties) {
		this.properties = properties;
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
	public StoredFile upload(FileUploadCommand command) {
		properties.assertAliyunOssConfigured();
		FileUploadProperties.AliyunOssConfig config = properties.getAliyunOss();
		OSS ossClient = new OSSClientBuilder().build(config.getEndpoint(), config.getAccessKeyId(),
				config.getAccessKeySecret());
		String contentType = CharSequenceUtil.blankToDefault(command.getFile().getContentType(), DEFAULT_CONTENT_TYPE);

		try (InputStream inputStream = command.getFile().getInputStream()) {
			PutObjectRequest request = new PutObjectRequest(config.getBucket(), command.getObjectKey(), inputStream);
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(contentType);
			request.setMetadata(metadata);
			PutObjectResult result = ossClient.putObject(request);

			return StoredFile.builder()
				.storagePlatform(platform())
				.bucket(config.getBucket())
				.objectKey(command.getObjectKey())
				.url(resolvePublicUrl(config, command.getObjectKey()))
				.originalName(command.getFile().getOriginalFilename())
				.extension(cn.hutool.core.io.FileUtil.extName(command.getFile().getOriginalFilename()))
				.contentType(contentType)
				.size(command.getFile().getSize())
				.etag(result.getETag())
				.build();
		}
		catch (Exception exception) {
			throw new FileException(FileUploadResultCode.FILE_UPLOAD_FAILED, platform().name(), exception.getMessage());
		}
		finally {
			ossClient.shutdown();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream download(String bucket, String objectKey) {
		properties.assertAliyunOssConfigured();
		FileUploadProperties.AliyunOssConfig config = properties.getAliyunOss();
		OSS ossClient = new OSSClientBuilder().build(config.getEndpoint(), config.getAccessKeyId(),
				config.getAccessKeySecret());
		try {
			OSSObject object = ossClient.getObject(bucket, objectKey);
			InputStream objectStream = object.getObjectContent();
			return new FilterInputStream(objectStream) {
				@Override
				public void close() throws IOException {
					try {
						super.close();
					}
					finally {
						ossClient.shutdown();
					}
				}
			};
		}
		catch (Exception exception) {
			ossClient.shutdown();
			throw new FileException(FileUploadResultCode.FILE_UPLOAD_FAILED, platform().name(), exception.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String presignGetUrl(String bucket, String objectKey, int expireSeconds) {
		properties.assertAliyunOssConfigured();
		FileUploadProperties.AliyunOssConfig config = properties.getAliyunOss();
		OSS ossClient = new OSSClientBuilder().build(config.getEndpoint(), config.getAccessKeyId(),
				config.getAccessKeySecret());
		try {
			Date expiration = Date.from(Instant.now().plusSeconds(expireSeconds));
			URL presignedUrl = ossClient.generatePresignedUrl(bucket, objectKey, expiration);
			return presignedUrl.toString();
		}
		catch (Exception exception) {
			throw new FileException(FileUploadResultCode.FILE_UPLOAD_FAILED, platform().name(), exception.getMessage());
		}
		finally {
			ossClient.shutdown();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(String bucket, String objectKey) {
		properties.assertAliyunOssConfigured();
		FileUploadProperties.AliyunOssConfig config = properties.getAliyunOss();
		OSS ossClient = new OSSClientBuilder().build(config.getEndpoint(), config.getAccessKeyId(),
				config.getAccessKeySecret());
		try {
			ossClient.deleteObject(bucket, objectKey);
		}
		catch (Exception exception) {
			if (StorageDeleteSupport.isOssObjectNotFound(exception)) {
				log.warn("OSS object already absent, skip delete: bucket={}, objectKey={}, reason={}", bucket,
						objectKey, exception.getMessage());
				return;
			}
			throw new FileException(FileUploadResultCode.FILE_UPLOAD_FAILED, platform().name(), exception.getMessage());
		}
		finally {
			ossClient.shutdown();
		}
	}

	/**
	 * 解析 OSS 对外访问地址
	 * @param config OSS 配置
	 * @param objectKey 对象键
	 * @return 访问地址
	 */
	private String resolvePublicUrl(FileUploadProperties.AliyunOssConfig config, String objectKey) {
		String objectPath = StorageUrlUtil.trimLeadingSlash(objectKey);
		if (CharSequenceUtil.isNotBlank(config.getPublicUrl())) {
			return StorageUrlUtil.concatPublicUrl(config.getPublicUrl(), objectPath);
		}

		// 统一采用 endpoint + bucket 的 path-style 语义，避免同方法内混用多种 URL 规则导致不可预测
		String endpointBaseUrl = StorageUrlUtil.normalizeHttpBaseUrl(config.getEndpoint());
		return StorageUrlUtil.concatPublicUrl(endpointBaseUrl + "/" + config.getBucket(), objectPath);
	}

}
