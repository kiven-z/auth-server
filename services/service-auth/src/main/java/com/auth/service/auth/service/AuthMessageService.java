package com.auth.service.auth.service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证消息应用服务：登录邮箱/短信验证码发送。
 *
 * @author Bunny
 */
public interface AuthMessageService {

	/**
	 * 发送登录邮箱验证码
	 * @param email 邮箱
	 * @param request 当前 HTTP 请求（用于解析登录 IP 等信息）
	 */
	void sendEmailCode(String email, HttpServletRequest request);

	/**
	 * 发送登录手机短信验证码
	 * @param phone 手机号
	 */
	void sendSmsCode(String phone);

}
