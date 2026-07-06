package com.auth.service.system.file.config.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * S3 协议统一配置：所有支持 S3 兼容 API 的存储平台都通过这份配置驱动
 *
 * @author Bunny
 */
@Getter
@Setter
public class S3PlatformProfile {

	/**
	 * 服务端点，形如 http(s)://xxx；用于 SDK 请求路由与 URL 拼接
	 */
	private String endpoint;

	/**
	 * region 名称（AWS/腾讯/华为等真实 region；S3 SDK 强制非空）
	 */
	private String region;

	private String accessKey;

	private String secretKey;

	private String bucket;

	/**
	 * 对外访问域名（可选，未配置时基于 endpoint + bucket 拼接）
	 */
	private String publicUrl;

	/**
	 * 是否使用 path-style（endpoint/bucket/key）；MinIO 与阿里云 OSS 需要 true，AWS S3 常为 false
	 */
	private boolean pathStyleAccess = true;

}
