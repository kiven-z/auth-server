package com.auth.service.auth.authentication.cookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

/**
 * 鉴权 Refresh Cookie 应用服务：组合策略与底层 HTTP 写入。
 *
 * @author Bunny
 */
@Service
public class AuthRefreshCookieService {

	private final RememberMeCookiePolicy rememberMeCookiePolicy;

	public AuthRefreshCookieService(RememberMeCookiePolicy rememberMeCookiePolicy) {
		this.rememberMeCookiePolicy = rememberMeCookiePolicy;
	}

	/**
	 * 从请求 Cookie 读取刷新令牌。
	 * @param request HTTP 请求
	 * @return 刷新令牌，缺失时为空串
	 */
	public String readRefreshToken(HttpServletRequest request) {
		return RefreshTokenCookieWriter.readToken(request);
	}

	/**
	 * 按「记住我」策略写入 Refresh Cookie。
	 * @param response HTTP 响应
	 * @param request HTTP 请求
	 * @param refreshToken 刷新令牌
	 * @param rememberMe 是否记住登录
	 */
	public void writeRefreshToken(HttpServletResponse response, HttpServletRequest request, String refreshToken,
			boolean rememberMe) {
		long maxAgeSeconds = rememberMeCookiePolicy.resolveMaxAgeSeconds(rememberMe);
		RefreshTokenCookieWriter.writeToken(response, request, refreshToken, maxAgeSeconds);
	}

	/**
	 * 清除 Refresh Cookie。
	 * @param response HTTP 响应
	 * @param request HTTP 请求
	 */
	public void clearRefreshToken(HttpServletResponse response, HttpServletRequest request) {
		RefreshTokenCookieWriter.clearToken(response, request);
	}

}
