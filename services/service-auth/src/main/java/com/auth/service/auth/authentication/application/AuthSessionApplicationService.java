package com.auth.service.auth.authentication.application;

import cn.hutool.core.util.BooleanUtil;
import com.auth.service.auth.authentication.api.dto.response.RefreshTokenResponse;
import com.auth.service.auth.authentication.api.dto.response.UserLoginResponse;
import com.auth.service.auth.authentication.application.assembler.RefreshResponseAssembler;
import com.auth.service.auth.authentication.application.input.credential.AbstractCredentialLogin;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditService;
import com.auth.service.auth.authentication.application.port.RefreshCookiePort;
import com.auth.service.auth.authentication.application.port.RememberMePolicyPort;
import com.auth.service.auth.authentication.domain.model.LogoutSessionHint;
import com.auth.service.auth.authentication.domain.model.RefreshSessionResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

	private final RefreshCookiePort refreshCookiePort;

	private final RememberMePolicyPort rememberMePolicyPort;

	private final LoginAuditService loginAuditService;

	public AuthSessionApplicationService(LoginApplicationService loginApplicationService,
			RefreshTokenService refreshTokenService, RefreshCookiePort refreshCookiePort,
			RememberMePolicyPort rememberMePolicyPort, LoginAuditService loginAuditService) {
		this.loginApplicationService = loginApplicationService;
		this.refreshTokenService = refreshTokenService;
		this.refreshCookiePort = refreshCookiePort;
		this.rememberMePolicyPort = rememberMePolicyPort;
		this.loginAuditService = loginAuditService;
	}

	/**
	 * 完成登录：签发令牌、写入 Redis 会话、下发 Refresh Cookie。
	 * @param credential 凭证登录入站模型（含 rememberMe）
	 * @param request HTTP 请求
	 * @param response HTTP 响应
	 * @return 登录响应（含 readMeDay）
	 */
	public UserLoginResponse completeLogin(AbstractCredentialLogin credential, HttpServletRequest request,
			HttpServletResponse response) {
		UserLoginResponse login = loginApplicationService.login(credential, request);

		refreshCookiePort.writeRefreshToken(response, request, login.getRefreshToken(),
				BooleanUtil.isTrue(credential.getRememberMe()));
		return login;
	}

	/**
	 * 完成刷新：旋转令牌、按会话 rememberMe 重写 Refresh Cookie。
	 * @param request HTTP 请求
	 * @param response HTTP 响应
	 * @return 刷新响应（含 readMeDay）
	 */
	public RefreshTokenResponse completeRefresh(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = refreshCookiePort.readRefreshToken(request);
		RefreshSessionResult result = refreshTokenService.refreshToken(refreshToken, request);
		RefreshTokenResponse refreshResponse = RefreshResponseAssembler.toRefreshTokenResponse(result);

		refreshResponse.setReadMeDay(rememberMePolicyPort.resolveReadMeDay());
		refreshCookiePort.writeRefreshToken(response, request, result.tokenPair().refreshToken(), result.rememberMe());
		return refreshResponse;
	}

	/**
	 * 完成登出：撤销服务端会话、写入单条登出审计并清除 Refresh Cookie。
	 * @param request HTTP 请求
	 * @param response HTTP 响应
	 */
	public void completeLogout(HttpServletRequest request, HttpServletResponse response) {
		try {
			Optional<LogoutSessionHint> auditHint = Optional.empty();

			String authHeader = request.getHeader("Authorization");
			if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
				String token = authHeader.substring(BEARER_PREFIX.length());
				auditHint = loginApplicationService.logout(token);
			}
			String refreshToken = refreshCookiePort.readRefreshToken(request);
			Optional<LogoutSessionHint> refreshHint = refreshTokenService.revokeByRefreshToken(refreshToken);
			auditHint = auditHint.or(() -> refreshHint);

			auditHint.ifPresent(hint -> loginAuditService.auditLogoutSuccess(request, hint.userId(), hint.jti()));
		}
		finally {
			refreshCookiePort.clearRefreshToken(response, request);
		}
	}

}
