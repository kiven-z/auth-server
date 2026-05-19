package com.auth.module.message.sender.modules.sms.dypns;

/**
 * 阿里云 Dypns 验证码短信发送 SPI（渠道 SMS_ALIYUN_DYPNS，非 dysmsapi 通用模板短信）
 *
 * @author Bunny
 */
public interface DypnsVerifyCodeSender extends AutoCloseable {

	/**
	 * 发送验证码短信（阿里云 Dypns SendSmsVerifyCode，签名见 auth.message.sms.sign-name）
	 * @param phone 手机号
	 * @param providerTemplateCode 厂商已审模板编码
	 * @param templateParamJson 模板变量 JSON 对象字符串
	 */
	void sendVerifyCode(String phone, String providerTemplateCode, String templateParamJson);

}
