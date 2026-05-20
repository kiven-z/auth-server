package com.auth.service.auth.authentication.infrastructure.cookie;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.util.Arrays;
import java.util.Optional;

/**
 * 刷新令牌 Cookie 底层读写（纯 HTTP，不含「记住我」策略）。
 *
 * @author Bunny
 */
@UtilityClass
public class RefreshTokenCookieWriter {

	/**
	 * Refresh Cookie 名称
	 */
	public static final String REFRESH_TOKEN_COOKIE_NAME = "auth-refresh-token";

	/**
	 * Spring {@link ResponseCookie} 会话级 Max-Age（浏览器关闭后失效）
	 */
	public static final long SESSION_COOKIE_MAX_AGE_SECONDS = -1L;

	/**
	 * 从 Cookie 中读取刷新令牌，不存在时返回空字符串交由业务层统一抛错。
	 * @param request HTTP 请求
	 * @return 刷新令牌
	 */
	public String readToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (ArrayUtil.isEmpty(cookies)) {
			return "";
		}
		Optional<Cookie> refreshCookie = Arrays.stream(cookies)
			.filter(cookie -> REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
			.findFirst();
		return refreshCookie.map(Cookie::getValue).orElse("");
	}

	/**
	 * 下发 HttpOnly 刷新令牌 Cookie。
	 * @param response HTTP 响应
	 * @param request HTTP 请求
	 * @param refreshToken 刷新令牌
	 * @param maxAgeSeconds Max-Age（秒）；{@link #SESSION_COOKIE_MAX_AGE_SECONDS} 表示会话 Cookie
	 */
	public void writeToken(HttpServletResponse response, HttpServletRequest request, String refreshToken,
			long maxAgeSeconds) {
		if (CharSequenceUtil.isBlank(refreshToken)) {
			return;
		}
		ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
			.httpOnly(true)
			.secure(resolveSecureFlag(request))
			.sameSite("Lax")
			.path("/api/auth")
			.maxAge(maxAgeSeconds)
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	/**
	 * 清除 HttpOnly 刷新令牌 Cookie。
	 * @param response HTTP 响应
	 * @param request HTTP 请求
	 */
	public void clearToken(HttpServletResponse response, HttpServletRequest request) {
		ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
			.httpOnly(true)
			.secure(resolveSecureFlag(request))
			.sameSite("Lax")
			.path("/api/auth")
			.maxAge(0)
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	/**
	 * 在 HTTPS 或网关透传 HTTPS 协议头时启用 Secure。
	 * @param request HTTP 请求
	 * @return 是否开启 Secure
	 */
	private boolean resolveSecureFlag(HttpServletRequest request) {
		if (request.isSecure()) {
			return true;
		}
		String forwardedProto = request.getHeader("X-Forwarded-Proto");
		return CharSequenceUtil.equalsIgnoreCase(forwardedProto, "https");
	}

}
