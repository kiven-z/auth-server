package com.auth.module.message.config.properties;

import cn.hutool.core.text.CharSequenceUtil;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 短信渠道配置（{@code auth.message.sms.*}）
 *
 * @author Bunny
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "auth.message.sms")
public class SmsProperties {

	/**
	 * 是否启用短信渠道（关闭时不创建阿里云客户端 Bean）
	 */
	private boolean enabled = false;

	/**
	 * 阿里云短信 / 号码认证配置
	 */
	private Aliyun aliyun = new Aliyun();

	/**
	 * 启用短信时校验必填项
	 */
	@PostConstruct
	public void validate() {
		if (!enabled) {
			return;
		}
		if (CharSequenceUtil.isBlank(aliyun.getAccessKeyId())
				|| CharSequenceUtil.isBlank(aliyun.getAccessKeySecret())) {
			throw new IllegalStateException(
					"auth.message.sms.aliyun.access-key-id and auth.message.sms.aliyun.access-key-secret are required when auth.message.sms.enabled=true.");
		}
	}

	/**
	 * 阿里云 SMS / Dypnsapi 连接参数
	 */
	@Getter
	@Setter
	public static class Aliyun {

		/**
		 * AccessKey ID
		 */
		private String accessKeyId;

		/**
		 * AccessKey Secret
		 */
		private String accessKeySecret;

		/**
		 * 区域 ID
		 */
		private String region = "cn-hangzhou";

		/**
		 * API Endpoint，参考 <a href="https://api.aliyun.com/product/Dypnsapi">Dypnsapi</a>
		 */
		private String endpoint = "dypnsapi.aliyuncs.com";

	}

}
