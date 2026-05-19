package com.auth.service.auth.authentication.facade;

import com.auth.service.auth.authentication.cookie.AuthRefreshCookieService;
import com.auth.service.auth.authentication.service.AuthService;
import com.auth.service.auth.authentication.service.RefreshTokenService;
import com.auth.service.auth.authentication.token.RefreshSessionResult;
import com.auth.service.auth.support.model.request.login.AbstractLoginRequest;
import com.auth.service.auth.support.model.response.RefreshTokenResponse;
import com.auth.service.auth.support.model.response.UserLoginResponse;
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
public class AuthSessionFacade {

	private final AuthService authService;

	private final RefreshTokenService refreshTokenService;

	private final AuthRefreshCookieService authRefreshCookieService;

	public AuthSessionFacade(AuthService authService, RefreshTokenService refreshTokenService,
			AuthRefreshCookieService authRefreshCookieService) {
		this.authService = authService;
		this.refreshTokenService = refreshTokenService;
		this.authRefreshCookieService = authRefreshCookieService;
	}

	/**
	 * 完成登录：签发令牌、写入 Redis 会话、下发 Refresh Cookie。
	 * @param auth 登录请求（含 rememberMe）
	 * @param request HTTP 请求
	 * @param response HTTP 响应
	 * @return 登录响应（含 readMeDay）
	 */
	public UserLoginResponse completeLogin(AbstractLoginRequest auth, HttpServletRequest request,
			HttpServletResponse response) {
		UserLoginResponse login = authService.login(auth, request);
		boolean rememberMe = Boolean.TRUE.equals(auth.getRememberMe());
		authRefreshCookieService.writeRefreshToken(response, request, login.getRefreshToken(), rememberMe);
		return login;
	}

	/**
	 * 完成刷新：旋转令牌、按会话 rememberMe 重写 Refresh Cookie。
	 * @param request HTTP 请求
	 * @param response HTTP 响应
	 * @return 刷新响应（含 readMeDay）
	 */
	public RefreshTokenResponse completeRefresh(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = authRefreshCookieService.readRefreshToken(request);
		RefreshSessionResult result = refreshTokenService.refreshToken(refreshToken, request);
		authRefreshCookieService.writeRefreshToken(response, request, result.response().getRefreshToken(),
				result.rememberMe());
		return result.response();
	}

	/**
	 * 完成登出：撤销服务端会话并清除 Refresh Cookie。
	 * @param request HTTP 请求
	 * @param response HTTP 响应
	 */
	public void completeLogout(HttpServletRequest request, HttpServletResponse response) {
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
			String token = authHeader.substring(BEARER_PREFIX.length());
			authService.logout(token, request);
		}
		authRefreshCookieService.clearRefreshToken(response, request);
	}

}
