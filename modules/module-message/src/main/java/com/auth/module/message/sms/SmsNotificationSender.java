package com.auth.module.message.sms;

/**
 * 标准模板短信发送 SPI（多厂商由 {@code module-message-sms4j} 等实现）
 *
 * @author Bunny
 */
public interface SmsNotificationSender {

	/**
	 * 按厂商模板发送通知类短信
	 * @param phone 手机号（不含国家码，由厂商 SDK 解释）
	 * @param providerTemplateCode 厂商模板 ID
	 * @param signName 短信签名（须与 sms4j blend 配置一致）
	 * @param templateParamJson 模板变量 JSON 对象字符串
	 * @param smsConfigId sms4j 配置 ID（{@code auth.message.sms.notification.blends} 的
	 * key），可为空则使用默认
	 */
	void sendTemplateSms(String phone, String providerTemplateCode, String signName, String templateParamJson,
			String smsConfigId);

}
