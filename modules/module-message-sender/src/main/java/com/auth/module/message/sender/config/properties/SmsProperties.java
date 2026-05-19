package com.auth.module.message.sender.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 短信渠道业务配置（{@code auth.message.sms.*}）
 * <p>
 * 阿里云 Dypns 验证码短信：AK、区域、Endpoint 与短信签名均在此配置。 仅在 {@code auth.message.sms.enabled=true}
 * 时由自动配置注册并校验。
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
	 * 是否启用 Dypns 验证码短信渠道（SMS_ALIYUN_DYPNS）
	 */
	private boolean enabled = false;

	/**
	 * 阿里云区域 ID
	 */
	@NotBlank
	private String region;

	/**
	 * Dypns API Endpoint
	 */
	@NotBlank
	private String endpoint;

	/**
	 * 阿里云 AccessKey ID
	 */
	@NotBlank
	private String accessKeyId;

	/**
	 * 阿里云 AccessKey Secret
	 */
	@NotBlank
	private String accessKeySecret;

	/**
	 * 短信签名（SendSmsVerifyCode 必填，须与号码认证控制台已审核签名一致）
	 */
	@NotBlank
	private String signName;

}
