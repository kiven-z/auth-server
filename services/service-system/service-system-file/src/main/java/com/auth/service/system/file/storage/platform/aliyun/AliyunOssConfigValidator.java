package com.auth.service.system.file.storage.platform.aliyun;

import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.config.properties.platform.AliyunOssStorageProperties;
import com.auth.service.system.file.storage.core.validator.StoragePlatformConfigValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 阿里云 OSS 平台配置校验器
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class AliyunOssConfigValidator implements StoragePlatformConfigValidator {

	private final FileUploadProperties properties;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validateOrThrow() {
		AliyunOssStorageProperties aliyunOss = Objects.requireNonNullElseGet(properties.getAliyunOss(),
				AliyunOssStorageProperties::new);
		assertNotBlank(aliyunOss.getEndpoint(), "aliyun-oss.endpoint");
		assertNotBlank(aliyunOss.getAccessKeyId(), "aliyun-oss.access-key-id");
		assertNotBlank(aliyunOss.getAccessKeySecret(), "aliyun-oss.access-key-secret");
		assertNotBlank(aliyunOss.getBucket(), "aliyun-oss.bucket");
	}

}
