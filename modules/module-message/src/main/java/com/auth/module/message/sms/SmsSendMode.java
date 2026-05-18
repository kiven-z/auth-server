package com.auth.module.message.sms;

/**
 * 短信发送模式：验证码走自研 Dypnsapi，通知类走 sms4j 等多厂商标准短信。
 *
 * @author Bunny
 */
public enum SmsSendMode {

	/**
	 * 验证码（阿里云号码认证 SendSmsVerifyCode）
	 */
	VERIFY,

	/**
	 * 通知/营销类标准模板短信（sms4j 等多厂商）
	 */
	NOTIFY

}
