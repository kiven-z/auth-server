package com.auth.module.message.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 短信配置属性绑定
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.message.sms")
public class SmsProperties {

	/**
	 * 阿里云 SMS AccessKey ID
	 */
	private String accessKeyId;

	/**
	 * 阿里云 SMS AccessKey Secret
	 */
	private String accessKeySecret;

}
