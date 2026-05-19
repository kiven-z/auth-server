package com.auth.service.auth.authentication.support;

import com.auth.service.auth.authentication.cookie.RememberMeCookiePolicy;
import com.auth.service.auth.support.model.response.RefreshTokenResponse;
import org.springframework.stereotype.Component;

/**
 * 为登录/刷新响应填充与 Nacos 配置一致的令牌元数据。
 *
 * @author Bunny
 */
@Component
public class AuthTokenResponseEnricher {

	private final RememberMeCookiePolicy rememberMeCookiePolicy;

	public AuthTokenResponseEnricher(RememberMeCookiePolicy rememberMeCookiePolicy) {
		this.rememberMeCookiePolicy = rememberMeCookiePolicy;
	}

	/**
	 * 填充 {@code readMeDay}（记住登录可选天数，来源于 {@code refreshExpired}）。
	 * @param response 登录或刷新响应
	 */
	public void enrichReadMeDay(RefreshTokenResponse response) {
		if (response == null) {
			return;
		}
		response.setReadMeDay(rememberMeCookiePolicy.resolveReadMeDay());
	}

}
