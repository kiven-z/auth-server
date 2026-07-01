package com.auth.service.system.message.config;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.common.exception.SystemBusinessException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;

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

	/**
	 * 断言字段不能为空
	 * @param value 字段值
	 * @param fieldName 字段名称
	 */
	private void assertNotBlank(String value, String fieldName) {
		if (CharSequenceUtil.isBlank(value)) {
			throw new SystemBusinessException(PARAM_REQUIRED, fieldName);
		}
	}

}
