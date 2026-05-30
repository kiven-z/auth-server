package com.auth.module.message.api.channel;

/**
 * 消息发送渠道
 *
 * @author Bunny
 */
public enum MessageChannel {

	/**
	 * 电子邮件
	 */
	EMAIL,

	/**
	 * 阿里云号码认证 Dypns 验证码短信（SendSmsVerifyCode；套餐/控制台模板）
	 */
	SMS_ALIYUN_DYPNS,

	// /**
	// * 阿里云 dysmsapi 自配模板短信（SendSms）；未来启用时再放开并实现对应 MessageSender
	// */
	// SMS_ALIYUN_DYSMSAPI,

	/**
	 * 钉钉
	 */
	DING_TALK,

	/**
	 * 站内信
	 */
	IN_APP,

}
