package com.auth.service.system.file.config.properties.platform;

import lombok.Getter;
import lombok.Setter;

/**
 * MinIO 存储配置
 *
 * @author Bunny
 */
@Getter
@Setter
public class MinioStorageProperties {

	private String endpoint;

	private String accessKey;

	private String secretKey;

	private String bucket;

	/**
	 * 对外访问域名（可选，不配置时回退 endpoint 拼接）
	 */
	private String publicUrl;

}
