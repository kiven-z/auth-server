package com.auth.service.auth.infrastructure.cookie.adapter;

import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.service.auth.application.port.login.RememberMePolicyPort;
import com.auth.service.auth.infrastructure.cookie.RefreshTokenCookieWriter;
import org.springframework.stereotype.Repository;

/**
 * 「记住我」与 Refresh Cookie / 响应 readMeDay 的策略（天数来源于 Nacos {@code refreshExpired}）。
 *
 * @author Bunny
 */
@Repository
public class RememberMePolicyAdapter implements RememberMePolicyPort {

	private static final long SECONDS_PER_DAY = 86_400L;

	private final JwtProperties jwtProperties;

	public RememberMePolicyAdapter(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long resolveMaxAgeSeconds(boolean rememberMe) {
		if (!rememberMe) {
			return RefreshTokenCookieWriter.SESSION_COOKIE_MAX_AGE_SECONDS;
		}
		return jwtProperties.getRefreshExpired();
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
