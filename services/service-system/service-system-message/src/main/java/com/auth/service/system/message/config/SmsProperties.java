package com.auth.service.system.message.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static com.auth.service.system.message.utils.MessageConfigAssertions.assertNotBlank;

/**
 * 短信渠道业务配置
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.message.sms")
@Configuration
public class SmsProperties {

	private String region;

	private String endpoint;

	private String accessKeyId;

	private String accessKeySecret;

	/**
	 * 短信签名
	 */
	private String signName;

	/**
	 * 断言发送短信所需配置齐全 发送时进行校验，防止发送未配置的短信
	 */
	public void assertSendRequiredConfigured() {
		assertNotBlank(region, "短信区域");
		assertNotBlank(endpoint, "短信服务地址");
		assertNotBlank(accessKeyId, "短信 AccessKeyId");
		assertNotBlank(accessKeySecret, "短信 AccessKeySecret");
		assertNotBlank(signName, "短信签名");
	}

}
