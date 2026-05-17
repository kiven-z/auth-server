package com.auth.service.auth.authentication.service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证消息服务
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

}
