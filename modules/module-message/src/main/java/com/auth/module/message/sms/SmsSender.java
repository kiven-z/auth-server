package com.auth.module.message.sms;

/**
 * 短信发送 SPI（由 {@code module-message-sms4j} 等实现）
 *
 * @author Bunny
 */
public interface SmsSender {

	/**
	 * 按厂商模板发送短信
	 * @param phone 手机号
	 * @param providerTemplateCode 厂商模板 ID
	 * @param signName 短信签名（须与 blend 配置一致）
	 * @param templateParamJson 模板变量 JSON 对象字符串
	 * @param smsConfigId sms4j blend 配置 ID（{@code sms.blends} 的 key），为空则使用默认
	 */
	void sendTemplateSms(String phone, String providerTemplateCode, String signName, String templateParamJson,
			String smsConfigId);

}
