package com.auth.module.message.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 短信渠道业务配置（{@code auth.message.sms.*}）
 * <p>
 * 厂商 AK、签名、blend 由 sms4j 原生 {@code sms.blends} 配置；此处仅保留开关与默认路由。
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
	 * 模板未指定 sms_config_id 时使用的默认 blend（对应 sms.blends 下的 configId）
	 */
	private String defaultConfigId;

	/**
	 * 默认短信签名（须与 {@code sms.blends.*.signature} 一致，用于日志与校验提示）
	 */
	private String defaultSignName;

}
