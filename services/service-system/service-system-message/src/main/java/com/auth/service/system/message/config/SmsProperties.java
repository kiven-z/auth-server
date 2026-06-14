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

	@NotBlank
	private String region;

	@NotBlank
	private String endpoint;

	@NotBlank
	private String accessKeyId;

	@NotBlank
	private String accessKeySecret;

	/**
	 * 短信签名
	 */
	@NotBlank
	private String signName;

}
