package com.auth.service.system.message.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * 短信渠道业务配置
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.message.sms")
@Configuration
@Validated
public class SmsProperties {

	/**
	 * 是否启用 Dypns 验证码短信渠道（SMS_ALIYUN_DYPNS，业务开关，配置项保留）
	 */
	private boolean enabled = true;

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
