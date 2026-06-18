package com.auth.service.auth.application.login.command;

import cn.hutool.core.util.BooleanUtil;
import com.auth.service.auth.application.login.LoginAuditService;
import com.auth.service.auth.application.login.input.BaseLoginCommand;
import com.auth.service.auth.application.port.login.RefreshCookiePort;
import com.auth.service.auth.application.port.login.RememberMePolicyPort;
import com.auth.service.auth.domain.authentication.model.CompletedLoginSession;
import com.auth.service.auth.domain.authentication.model.CompletedRefreshSession;
import com.auth.service.auth.domain.authentication.model.LogoutSessionHint;
import com.auth.service.auth.domain.authentication.model.RefreshSessionResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.auth.common.jwt.provider.AbstractJwtTokenProvider.BEARER_PREFIX;

/**
 * HTTP 会话编排：登录/刷新/登出的业务调用与 Refresh Cookie 写入。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class AuthSessionApplicationService {

	private final LoginApplicationService loginApplicationService;

	private final RefreshTokenApplicationService refreshTokenService;

	private final RefreshCookiePort refreshCookiePort;

	private final RememberMePolicyPort rememberMePolicyPort;

	private final LoginAuditService loginAuditService;

	/**
	 * 完成登录：签发令牌、写入 Redis 会话、下发 Refresh Cookie。
	 * @param command 登录命令（含 rememberMe）
	 * @param request HTTP 请求
	 * @param response HTTP 响应
	 * @return 登录会话读模型
	 */
	public CompletedLoginSession completeLogin(BaseLoginCommand command, HttpServletRequest request,
			HttpServletResponse response) {
		CompletedLoginSession session = loginApplicationService.login(command, request);
		refreshCookiePort.writeRefreshToken(response, request, session.getRefreshToken(),
				BooleanUtil.isTrue(command.getRememberMe()));
		return session;
	}

	/**
	 * 完成刷新：旋转令牌、按会话 rememberMe 重写 Refresh Cookie。
	 * @param request HTTP 请求
	 * @param response HTTP 响应
	 * @return 刷新会话读模型
	 */
	public CompletedRefreshSession completeRefresh(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = refreshCookiePort.readRefreshToken(request);
		RefreshSessionResult result = refreshTokenService.refreshToken(refreshToken, request);

		CompletedRefreshSession session = new CompletedRefreshSession();
		session.setAccessToken(result.tokenPair().accessToken());
		session.setRefreshToken(result.tokenPair().refreshToken());
		session.setExpires(result.tokenPair().accessExpiresAt());
		session.setReadMeDay(rememberMePolicyPort.resolveReadMeDay());

		refreshCookiePort.writeRefreshToken(response, request, result.tokenPair().refreshToken(), result.rememberMe());
		return session;
	}

	/**
	 * 完成登出：撤销服务端会话、写入单条登出审计并清除 Refresh Cookie。
	 * @param request HTTP 请求
	 * @param response HTTP 响应
	 */
	public void completeLogout(HttpServletRequest request, HttpServletResponse response) {
		try {
			Optional<LogoutSessionHint> auditHint = Optional.empty();
			String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

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
