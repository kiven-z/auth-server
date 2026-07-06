package com.auth.service.system.file.config.properties.platform;

import lombok.Getter;
import lombok.Setter;

/**
 * 阿里云 OSS 存储配置
 *
 * @author Bunny
 */
@Getter
@Setter
public class AliyunOssStorageProperties {

	private String endpoint;

	private String accessKeyId;

	private String accessKeySecret;

	private String bucket;

	/**
	 * 访问域名（可选，不配置时按 endpoint 生成）
	 */
	private String publicUrl;

}
