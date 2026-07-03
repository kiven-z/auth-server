package com.auth.service.system.file.storage.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.FileUploadProperties;
import com.auth.service.system.file.exception.FileException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.model.value.FileUploadCommand;
import com.auth.service.system.file.model.value.StoredFile;
import com.auth.service.system.file.storage.FileStorageProvider;
import com.auth.service.system.file.utils.StorageUrlUtil;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * 阿里云 OSS 文件上传实现
 *
 * @author Bunny
 */
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
