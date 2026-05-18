package com.auth.module.message.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 短信渠道业务配置（{@code auth.message.sms.*}）
 * <p>
 * 阿里云 Dypns 验证码短信：AK、区域、Endpoint 与短信签名均在此配置。
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
	 * 是否启用 Dypns 验证码短信渠道（SMS_DYPNS_VERIFY_CODE）
	 */
	private boolean enabled = false;

	/**
	 * 阿里云区域 ID
	 */
	private String region;

	/**
	 * Dypns API Endpoint
	 */
	private String endpoint;

	/**
	 * 阿里云 AccessKey ID
	 */
	private String accessKeyId;

	/**
	 * 阿里云 AccessKey Secret
	 */
	private String accessKeySecret;

	/**
	 * 短信签名（SendSmsVerifyCode 必填，须与号码认证控制台已审核签名一致）
	 */
	private String signName;

}
