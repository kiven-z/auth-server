package com.auth.service.auth.authentication.application.impl;

import com.auth.service.auth.authentication.api.assembler.LoginCommandAssembler;
import com.auth.service.auth.authentication.api.dto.request.AbstractLoginRequest;
import com.auth.service.auth.authentication.api.dto.response.RefreshTokenResponse;
import com.auth.service.auth.authentication.api.dto.response.UserLoginResponse;
import com.auth.service.auth.authentication.application.AuthService;
import com.auth.service.auth.authentication.application.AuthSessionApplicationService;
import com.auth.service.auth.authentication.application.RefreshTokenService;
import com.auth.service.auth.authentication.domain.model.RefreshSessionResult;
import com.auth.service.auth.authentication.infrastructure.cookie.AuthRefreshCookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import static com.auth.common.jwt.provider.AbstractJwtTokenProvider.BEARER_PREFIX;

/**
 * HTTP 会话编排：登录/刷新/登出的业务调用与 Refresh Cookie 写入。
 *
 * @author Bunny
 */
@Service
public class AuthSessionApplicationServiceImpl implements AuthSessionApplicationService {

	private final AuthService authService;

	private final RefreshTokenService refreshTokenService;

	private final AuthRefreshCookieService authRefreshCookieService;

	public AuthSessionApplicationServiceImpl(AuthService authService, RefreshTokenService refreshTokenService,
			AuthRefreshCookieService authRefreshCookieService) {
		this.authService = authService;
		this.refreshTokenService = refreshTokenService;
		this.authRefreshCookieService = authRefreshCookieService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserLoginResponse completeLogin(AbstractLoginRequest auth, HttpServletRequest request,
			HttpServletResponse response) {
		UserLoginResponse login = authService.login(LoginCommandAssembler.toCommand(auth), request);
		boolean rememberMe = Boolean.TRUE.equals(auth.getRememberMe());
		authRefreshCookieService.writeRefreshToken(response, request, login.getRefreshToken(), rememberMe);
		return login;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RefreshTokenResponse completeRefresh(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = authRefreshCookieService.readRefreshToken(request);
		RefreshSessionResult result = refreshTokenService.refreshToken(refreshToken, request);
		authRefreshCookieService.writeRefreshToken(response, request, result.response().getRefreshToken(),
				result.rememberMe());
		return result.response();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void completeLogout(HttpServletRequest request, HttpServletResponse response) {
		try {
			String authHeader = request.getHeader("Authorization");
			if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
				String token = authHeader.substring(BEARER_PREFIX.length());
				authService.logout(token, request);
			}
			String refreshToken = authRefreshCookieService.readRefreshToken(request);
			refreshTokenService.revokeByRefreshToken(refreshToken, request);
		}
		finally {
			authRefreshCookieService.clearRefreshToken(response, request);
		}
	}

}
