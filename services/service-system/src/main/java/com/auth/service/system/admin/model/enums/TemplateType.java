package com.auth.service.system.admin.model.enums;

import lombok.Getter;

/**
 * 邮件模板类型枚举
 *
 * @author Bunny
 */
@Getter
public enum TemplateType {

	/**
	 * 欢迎邮件模板
	 */
	WELCOME("welcome", "欢迎邮件"),

	/**
	 * 登录邮件验证码模板
	 */
	LOGIN_EMAIL_CODE("login-email-code", "登录邮件验证码"),

	/**
	 * 登录成功邮件模板
	 */
	RESET_PASSWORD_SUCCESS("reset-password-success", "重置密码成功"),

	/**
	 * 密码重置邮件模板
	 */
	RESET_PASSWORD("reset-password", "重置密码"),

	/**
	 * 通用通知邮件模板
	 */
	NOTIFICATION("notification", "通知");

	/**
	 * 模板编码
	 */
	private final String templateCode;

	/**
	 * 模板描述
	 */
	private final String description;

	/**
	 * 构造邮件模板类型
	 * @param templateCode 模板名称
	 * @param description 模板描述
	 */
	TemplateType(String templateCode, String description) {
		this.templateCode = templateCode;
		this.description = description;
	}

}
