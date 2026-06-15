package com.auth.service.auth.application.port.login;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Refresh Token HTTP Cookie 读写端口。
 *
 * @author Bunny
 */
public interface RefreshCookiePort {

	/**
	 * 从请求 Cookie 读取刷新令牌。
	 * @param request HTTP 请求
	 * @return 刷新令牌，缺失时为空串
	 */
	String readRefreshToken(HttpServletRequest request);

	/**
	 * 按「记住我」策略写入 Refresh Cookie。
	 * @param response HTTP 响应
	 * @param request HTTP 请求
	 * @param refreshToken 刷新令牌
	 * @param rememberMe 是否记住登录
	 */
	void writeRefreshToken(HttpServletResponse response, HttpServletRequest request, String refreshToken,
			boolean rememberMe);

	/**
	 * 清除 Refresh Cookie。
	 * @param response HTTP 响应
	 * @param request HTTP 请求
	 */
	void clearRefreshToken(HttpServletResponse response, HttpServletRequest request);

}
