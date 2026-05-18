package com.auth.module.message.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 短信渠道配置（{@code auth.message.sms.*}，厂商发送由 sms4j 适配）
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
	 * 模板未指定 sms_config_id 时使用的默认 blend
	 */
	private String defaultConfigId;

	/**
	 * sms4j 多厂商配置（key 即 configId）
	 */
	private Map<String, SmsBlendProperties> blends = new LinkedHashMap<>();

}
