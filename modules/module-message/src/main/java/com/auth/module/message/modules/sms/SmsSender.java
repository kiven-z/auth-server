package com.auth.module.message.modules.sms;

/**
 * 短信发送 SPI（由阿里云 Dypns 等厂商适配器实现）
 *
 * @author Bunny
 */
public interface SmsSender {

	/**
	 * 发送验证码短信（阿里云 Dypns SendSmsVerifyCode）
	 * @param phone 手机号
	 * @param signName 短信签名（须与控制台一致）
	 * @param providerTemplateCode 厂商已审模板编码
	 * @param templateParamJson 模板变量 JSON 对象字符串
	 */
	void sendVerifyCode(String phone, String signName, String providerTemplateCode, String templateParamJson);

}
