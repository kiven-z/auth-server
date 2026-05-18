package com.auth.module.message.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 短信渠道配置（{@code auth.message.sms.*}）
 * <p>
 * 验证码：{@code provider} 路由到对应厂商的独立 Properties（如 {@code AliyunSmsProperties}）。<br>
 * 通知类：{@code notification.*}（sms4j 多厂商，见 {@code module-message-sms4j}）。
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
	 * 是否启用短信渠道（统一派发入口）
	 */
	private boolean enabled = false;

	/**
	 * 是否启用验证码短信（Dypnsapi）；为 false 时不校验/不装配 verify 客户端
	 */
	private boolean verifyEnabled = true;

	/**
	 * 验证码短信服务商
	 */
	private Provider provider = Provider.ALIYUN;

	/**
	 * 通知类标准短信（sms4j）
	 */
	private Notification notification = new Notification();

	/**
	 * 短信服务商（验证码通道）
	 */
	public enum Provider {

		/**
		 * 阿里云
		 */
		ALIYUN,

		/**
		 * 腾讯云
		 */
		TENCENT

	}

	/**
	 * 通知类短信（sms4j）配置
	 */
	@Getter
	@Setter
	public static class Notification {

		/**
		 * 是否启用 sms4j 通知短信
		 */
		private boolean enabled = false;

		/**
		 * 模板未指定 sms_config_id 时使用的默认 blend
		 */
		private String defaultConfigId;

		/**
		 * sms4j 多厂商配置（key 即 configId）
		 */
		private Map<String, SmsNotificationBlendProperties> blends = new LinkedHashMap<>();

	}

}
