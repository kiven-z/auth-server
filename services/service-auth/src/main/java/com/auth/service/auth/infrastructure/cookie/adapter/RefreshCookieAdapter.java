package com.auth.service.auth.infrastructure.cookie.adapter;

import com.auth.service.auth.application.port.login.RefreshCookiePort;
import com.auth.service.auth.application.port.login.RememberMePolicyPort;
import com.auth.service.auth.infrastructure.cookie.RefreshTokenCookieWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Repository;

/**
 * Refresh Cookie 端口适配器：组合策略与底层 HTTP 写入。
 *
 * @author Bunny
 */
@Repository
public class RefreshCookieAdapter implements RefreshCookiePort {

	private final RememberMePolicyPort rememberMePolicyPort;

	public RefreshCookieAdapter(RememberMePolicyPort rememberMePolicyPort) {
		this.rememberMePolicyPort = rememberMePolicyPort;
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
		long maxAgeSeconds = rememberMePolicyPort.resolveMaxAgeSeconds(rememberMe);
		RefreshTokenCookieWriter.writeToken(response, request, refreshToken, maxAgeSeconds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearRefreshToken(HttpServletResponse response, HttpServletRequest request) {
		RefreshTokenCookieWriter.clearToken(response, request);
	}

}
