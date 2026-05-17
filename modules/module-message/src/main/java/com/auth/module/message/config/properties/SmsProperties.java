package com.auth.module.message.config.properties;

import cn.hutool.core.text.CharSequenceUtil;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 短信渠道配置（{@code auth.message.sms.*}）
 * <p>
 * 切换厂商：仅改 {@code provider} 与对应子节点（{@code aliyun} / {@code tencent}），并启用对应客户端配置类。
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
	 * 短信服务商（与 {@code auth.message.sms.provider} 对应）
	 */
	private Provider provider = Provider.ALIYUN;

	/**
	 * 阿里云配置（{@code provider=aliyun} 时生效）
	 */
	private Aliyun aliyun = new Aliyun();

	/**
	 * 腾讯云配置（{@code provider=tencent} 时生效）
	 */
	private Tencent tencent = new Tencent();

	/**
	 * 启用时按 {@link #provider} 校验对应厂商必填项
	 */
	@PostConstruct
	public void validate() {
		if (!enabled) {
			return;
		}
		switch (provider) {
			// 阿里云
			case ALIYUN -> validateAliyun();
			// 腾讯云
			case TENCENT -> validateTencent();
			// 默认
			default -> validateAliyun();
		}
	}

	/**
	 * 校验阿里云配置
	 */
	private void validateAliyun() {
		if (CharSequenceUtil.isBlank(aliyun.getAccessKeyId())
				|| CharSequenceUtil.isBlank(aliyun.getAccessKeySecret())) {
			throw new IllegalStateException(
					"auth.message.sms.aliyun.access-key-id and access-key-secret are required when provider=aliyun.");
		}
	}

	/**
	 * 校验腾讯云配置
	 */
	private void validateTencent() {
		throw new IllegalStateException("auth.message.sms.provider=tencent is not implemented yet.");
	}

	/**
	 * 短信服务商
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
	 * 阿里云 SMS / Dypnsapi 连接参数
	 */
	@Getter
	@Setter
	public static class Aliyun {

		private String accessKeyId;

		private String accessKeySecret;

		private String region = "cn-hangzhou";

		private String endpoint = "dypnsapi.aliyuncs.com";

		/**
		 * 默认短信签名（模板未配置 sign_name 时使用）
		 */
		private String defaultSignName;

		/**
		 * 默认 Dypnsapi 方案名称
		 */
		private String defaultSchemeName;

	}

	/**
	 * 腾讯云短信连接参数（预留）
	 */
	@Getter
	@Setter
	public static class Tencent {

		private String secretId;

		private String secretKey;

		private String region = "ap-guangzhou";

	}

}
