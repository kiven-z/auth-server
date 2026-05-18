package com.auth.module.message.config.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * sms4j 单厂商 blend 配置（映射 {@code auth.message.sms.blends.*}）
 *
 * @author Bunny
 */
@Getter
@Setter
public class SmsBlendProperties {

	/**
	 * 是否启用该 blend
	 */
	private boolean enabled = true;

	/**
	 * 厂商标识（与 sms4j supplier 一致，如 alibaba、tencent）
	 */
	private String supplier;

	/**
	 * AccessKey / SecretId
	 */
	private String accessKeyId;

	/**
	 * AccessKeySecret / SecretKey
	 */
	private String accessKeySecret;

	/**
	 * 短信签名
	 */
	private String signature;

	/**
	 * 腾讯云 sdkAppId（supplier=tencent 时必填）
	 */
	private String sdkAppId;

}
