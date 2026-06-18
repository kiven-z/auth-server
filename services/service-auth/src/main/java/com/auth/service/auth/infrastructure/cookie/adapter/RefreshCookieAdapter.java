package com.auth.service.auth.infrastructure.cookie.adapter;

import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.service.auth.application.port.login.RefreshCookiePort;
import com.auth.service.auth.infrastructure.cookie.RefreshTokenCookieWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Repository;

/**
 * Refresh Cookie 端口适配器：读写 Cookie 并解析「记住我」策略。
 *
 * @author Bunny
 */
@Repository
public class RefreshCookieAdapter implements RefreshCookiePort {

	private static final long SECONDS_PER_DAY = 86_400L;

	private final JwtProperties jwtProperties;

	public RefreshCookieAdapter(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String readRefreshToken(HttpServletRequest request) {
		return RefreshTokenCookieWriter.readToken(request);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeRefreshToken(HttpServletResponse response, HttpServletRequest request, String refreshToken,
			boolean rememberMe) {
		long maxAgeSeconds = !rememberMe ? RefreshTokenCookieWriter.SESSION_COOKIE_MAX_AGE_SECONDS
				: jwtProperties.getRefreshExpired();

		RefreshTokenCookieWriter.writeToken(response, request, refreshToken, maxAgeSeconds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearRefreshToken(HttpServletResponse response, HttpServletRequest request) {
		RefreshTokenCookieWriter.clearToken(response, request);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long resolveReadMeDay() {
		long refreshExpiredSeconds = jwtProperties.getRefreshExpired();
		if (refreshExpiredSeconds <= 0) {
			return 1L;
		}
		return (refreshExpiredSeconds + SECONDS_PER_DAY - 1) / SECONDS_PER_DAY;
	}

}
