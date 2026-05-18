package com.auth.module.message.sms.aliyun;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云 Dypnsapi 连接参数（{@code auth.message.sms.aliyun.*}）
 * <p>
 * 仅在 {@code auth.message.sms.provider=aliyun} 时由 {@link AliyunSmsAutoConfiguration} 加载。
 * </p>
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.message.sms.aliyun")
public class AliyunSmsProperties {

	/**
	 * 阿里云 AccessKey ID
	 */
	private String accessKeyId;

	/**
	 * 阿里云 AccessKey Secret
	 */
	private String accessKeySecret;

	/**
	 * 地域
	 */
	private String region = "cn-hangzhou";

	/**
	 * 服务端点
	 */
	private String endpoint = "dypnsapi.aliyuncs.com";

	/**
	 * 默认短信签名（模板未配置 sign_name 时使用）
	 */
	private String defaultSignName;

	/**
	 * 默认 Dypnsapi 方案名称
	 */
	private String defaultSchemeName;

}
