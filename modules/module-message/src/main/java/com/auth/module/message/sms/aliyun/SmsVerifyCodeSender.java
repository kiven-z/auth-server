package com.auth.module.message.sms.aliyun;

/**
 * 短信验证码发送 SPI（厂商实现由 module-message 自动配置注册）
 *
 * @author Bunny
 */
public interface SmsVerifyCodeSender {

	/**
	 * 发送验证码短信
	 * @param phone 手机号（不含国家码）
	 * @param providerTemplateCode 厂商模板 CODE
	 * @param signName 短信签名
	 * @param templateParam 模板变量 JSON
	 * @param countryCode 国家码
	 * @param schemeName 方案名称（阿里云可选）
	 */
	void sendVerifyCode(String phone, String providerTemplateCode, String signName, String templateParam,
			String countryCode, String schemeName);

}
