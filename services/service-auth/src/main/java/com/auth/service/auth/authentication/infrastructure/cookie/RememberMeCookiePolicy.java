package com.auth.service.auth.authentication.infrastructure.cookie;

import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.service.auth.authentication.application.port.RememberMePolicyPort;
import org.springframework.stereotype.Component;

/**
 * 「记住我」与 Refresh Cookie / 响应 readMeDay 的策略（天数来源于 Nacos {@code refreshExpired}）。
 *
 * @author Bunny
 */
@Component
public class RememberMeCookiePolicy implements RememberMePolicyPort {

	private static final long SECONDS_PER_DAY = 86_400L;

	private final JwtProperties jwtProperties;

	public RememberMeCookiePolicy(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
	}

	/**
	 * 解析 Refresh Cookie 的 Max-Age（秒）。
	 * @param rememberMe 是否记住登录
	 * @return 勾选时为 {@code refreshExpired}；未勾选时为会话 Cookie
	 */
	@Override
	public long resolveMaxAgeSeconds(boolean rememberMe) {
		if (!rememberMe) {
			return RefreshTokenCookieWriter.SESSION_COOKIE_MAX_AGE_SECONDS;
		}
		return jwtProperties.getRefreshExpired();
	}

	/**
	 * 解析响应中的「阅读/记住」天数（与前端 multiple-tabs expires 对齐）。
	 * @return {@code refreshExpired} 换算的天数（不足一天按 1 天计）
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
