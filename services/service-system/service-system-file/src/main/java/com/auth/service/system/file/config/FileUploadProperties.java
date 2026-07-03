package com.auth.service.system.file.config;

import com.auth.module.file.api.enums.StoragePlatformEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.auth.service.system.file.utils.FileConfigAssertions.assertNotBlank;

/**
 * 文件上传配置
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.file")
@Configuration
public class FileUploadProperties {

	/**
	 * 默认上传平台（请求未指定时生效）
	 */
	private StoragePlatformEnum defaultPlatform = StoragePlatformEnum.MINIO;

	private MinioConfig minio = new MinioConfig();

	private AliyunOssConfig aliyunOss = new AliyunOssConfig();

	/**
	 * 访问地址签名相关配置
	 */
	private UrlConfig url = new UrlConfig();

	/**
	 * 文件预览策略配置
	 */
	private PreviewConfig preview = new PreviewConfig();

	/**
	 * 断言 MinIO 配置齐全
	 */
	public void assertMinioConfigured() {
		assertNotBlank(minio.getEndpoint(), "minio.endpoint");
		assertNotBlank(minio.getAccessKey(), "minio.access-key");
		assertNotBlank(minio.getSecretKey(), "minio.secret-key");
		assertNotBlank(minio.getBucket(), "minio.bucket");
	}

	/**
	 * 断言阿里云 OSS 配置齐全
	 */
	public void assertAliyunOssConfigured() {
		assertNotBlank(aliyunOss.getEndpoint(), "aliyun-oss.endpoint");
		assertNotBlank(aliyunOss.getAccessKeyId(), "aliyun-oss.access-key-id");
		assertNotBlank(aliyunOss.getAccessKeySecret(), "aliyun-oss.access-key-secret");
		assertNotBlank(aliyunOss.getBucket(), "aliyun-oss.bucket");
	}

	/**
	 * MinIO 配置
	 */
	@Getter
	@Setter
	public static class MinioConfig {

		private String endpoint;

		private String accessKey;

		private String secretKey;

		private String bucket;

		/**
		 * 对外访问域名（可选，不配置时回退 endpoint 拼接）
		 */
		private String publicUrl;

	}

	/**
	 * 阿里云 OSS 配置
	 */
	@Getter
	@Setter
	public static class AliyunOssConfig {

		private String endpoint;

		private String accessKeyId;

		private String accessKeySecret;

		private String bucket;

		/**
		 * 访问域名（可选，不配置时按 endpoint 生成）
		 */
		private String publicUrl;

	}

	/**
	 * 临时访问地址配置
	 */
	@Getter
	@Setter
	public static class UrlConfig {

		/**
		 * 临时访问地址过期秒数
		 */
		private Integer expireSeconds = 300;

	}

	/**
	 * 文件预览限制配置
	 */
	@Getter
	@Setter
	public static class PreviewConfig {

		/**
		 * 允许预览的内容类型白名单
		 */
		private List<String> allowedContentTypes = List.of("image/*", "application/pdf");

	}

}
