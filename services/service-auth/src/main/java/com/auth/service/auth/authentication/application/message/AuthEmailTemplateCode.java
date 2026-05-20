package com.auth.service.auth.authentication.application.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 认证服务使用的邮件模板编码（与 sys_message_template.scene_code、channel=EMAIL 约定一致）
 *
 * @author Bunny
 */
@Getter
@RequiredArgsConstructor
public enum AuthEmailTemplateCode {

	/**
	 * 登录邮箱验证码
	 */
	LOGIN_EMAIL_CODE("login-email-code"),

	/**
	 * 忘记密码 / 重置密码
	 */
	RESET_PASSWORD("reset-password");

	/**
	 * 数据库模板编码
	 */
	private final String templateCode;

}
