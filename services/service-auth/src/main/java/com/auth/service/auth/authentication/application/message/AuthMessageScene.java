package com.auth.service.auth.authentication.application.message;

import com.auth.module.message.api.channel.MessageChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 认证模块消息场景（与 sys_message_template.scene_code、发送 channel 约定一致）。
 *
 * @author Bunny
 */
@Getter
@RequiredArgsConstructor
public enum AuthMessageScene {

	/**
	 * 登录邮箱验证码
	 */
	LOGIN_EMAIL(MessageChannel.EMAIL, "login-email-code"),

	/**
	 * 登录短信验证码（阿里云 Dypns）
	 */
	LOGIN_SMS(MessageChannel.SMS_ALIYUN_DYPNS, "login-sms-code"),

	/**
	 * 忘记密码 / 重置密码（预留）
	 */
	RESET_PASSWORD(MessageChannel.EMAIL, "reset-password");

	/**
	 * 发送渠道
	 */
	private final MessageChannel channel;

	/**
	 * 模板/场景编码
	 */
	private final String templateCode;

}
