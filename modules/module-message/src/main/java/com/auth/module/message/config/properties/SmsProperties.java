package com.auth.module.message.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 短信渠道业务配置（{@code auth.message.sms.*}）
 * <p>
 * 阿里云 Dypns 验证码短信：AK、签名、区域与 Endpoint 均在此配置。
 * </p>
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.message.sms")
@Validated
public class SmsProperties {

	/**
	 * 是否启用短信渠道
	 */
	private boolean enabled = false;

	/**
	 * 阿里云区域 ID
	 */
	private String region = "cn-hangzhou";

	/**
	 * Dypns API Endpoint
	 */
	private String endpoint = "dypnsapi.aliyuncs.com";

	/**
	 * 短信签名（须与阿里云控制台一致）
	 */
	private String signName;

	/**
	 * 阿里云 AccessKey ID
	 */
	private String accessKeyId;

	/**
	 * 阿里云 AccessKey Secret
	 */
	private String accessKeySecret;

}
