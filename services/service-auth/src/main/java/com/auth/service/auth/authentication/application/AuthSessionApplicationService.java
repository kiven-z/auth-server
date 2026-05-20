package com.auth.service.auth.authentication.application;

import cn.hutool.core.util.BooleanUtil;
import com.auth.service.auth.authentication.api.assembler.LoginCommandAssembler;
import com.auth.service.auth.authentication.api.dto.request.AbstractLoginRequest;
import com.auth.service.auth.authentication.api.dto.response.RefreshTokenResponse;
import com.auth.service.auth.authentication.api.dto.response.UserLoginResponse;
import com.auth.service.auth.authentication.application.assembler.RefreshResponseAssembler;
import com.auth.service.auth.authentication.domain.model.RefreshSessionResult;
import com.auth.service.auth.authentication.infrastructure.cookie.AuthRefreshCookieService;
import com.auth.service.auth.authentication.infrastructure.cookie.RememberMeCookiePolicy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import static com.auth.common.jwt.provider.AbstractJwtTokenProvider.BEARER_PREFIX;

/**
 * HTTP 会话编排：登录/刷新/登出的业务调用与 Refresh Cookie 写入
 *
 * @author Bunny
 */
@Service
public class AuthSessionApplicationService {

	private final LoginApplicationService loginApplicationService;

	private final RefreshTokenService refreshTokenService;

	private final AuthRefreshCookieService authRefreshCookieService;

	private final RememberMeCookiePolicy rememberMeCookiePolicy;

	public AuthSessionApplicationService(LoginApplicationService loginApplicationService,
			RefreshTokenService refreshTokenService, AuthRefreshCookieService authRefreshCookieService,
			RememberMeCookiePolicy rememberMeCookiePolicy) {
		this.loginApplicationService = loginApplicationService;
		this.refreshTokenService = refreshTokenService;
		this.authRefreshCookieService = authRefreshCookieService;
		this.rememberMeCookiePolicy = rememberMeCookiePolicy;
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
		UserLoginResponse login = loginApplicationService.login(LoginCommandAssembler.toCommand(auth), request);

		authRefreshCookieService.writeRefreshToken(response, request, login.getRefreshToken(),
				BooleanUtil.isTrue(auth.getRememberMe()));
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
		RefreshTokenResponse refreshResponse = RefreshResponseAssembler.toRefreshTokenResponse(result);

		refreshResponse.setReadMeDay(rememberMeCookiePolicy.resolveReadMeDay());
		authRefreshCookieService.writeRefreshToken(response, request, result.tokenPair().refreshToken(),
				result.rememberMe());
		return refreshResponse;
	}

	/**
	 * 完成登出：撤销服务端会话并清除 Refresh Cookie。
	 * @param request HTTP 请求
	 * @param response HTTP 响应
	 */
	public void completeLogout(HttpServletRequest request, HttpServletResponse response) {
		try {
			String authHeader = request.getHeader("Authorization");
			if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
				String token = authHeader.substring(BEARER_PREFIX.length());
				loginApplicationService.logout(token, request);
			}
			String refreshToken = authRefreshCookieService.readRefreshToken(request);
			refreshTokenService.revokeByRefreshToken(refreshToken, request);
		}
		finally {
			authRefreshCookieService.clearRefreshToken(response, request);
		}
	}

}
