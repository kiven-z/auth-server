package com.auth.service.auth.service.impl;

import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.autoconfig.web.SecurityUserUtils;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.infrastructure.login.*;
import com.auth.service.auth.infrastructure.login.strategy.LoginStrategy;
import com.auth.service.auth.infrastructure.profile.AuthProfileCache;
import com.auth.service.auth.infrastructure.session.SessionLimitManager;
import com.auth.service.auth.infrastructure.session.UserSessionIndexService;
import com.auth.service.auth.infrastructure.session.UserSessionIndexSession;
import com.auth.service.auth.model.enums.AuthLoginLogResult;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.request.AbstractAuthentication;
import com.auth.service.auth.model.response.UserLoginResponse;
import com.auth.service.auth.service.AuthLoginAuditService;
import com.auth.service.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 *
 * @author Bunny
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

	private final Map<Class<? extends AbstractAuthentication>, LoginStrategy<?>> strategyRegistry;

	private final AuthProfileCache authProfileCache;

	private final UserSessionIndexService userSessionIndexService;

	private final UserSessionIndexSession userSessionIndexSession;

	private final SessionLimitManager sessionLimitManager;

	private final TokenService tokenService;

	private final LoginFailureRateLimiter loginFailureRateLimiter;

	private final AuthLoginAuditService loginLogService;

	public AuthServiceImpl(List<LoginStrategy<?>> strategies, AuthProfileCache authProfileCache,
			UserSessionIndexService userSessionIndexService, UserSessionIndexSession userSessionIndexSession,
			SessionLimitManager sessionLimitManager, TokenService tokenService,
			LoginFailureRateLimiter loginFailureRateLimiter, AuthLoginAuditService loginLogService) {
		this.strategyRegistry = strategies.stream()
			.collect(Collectors.toUnmodifiableMap(LoginStrategy::supports, Function.identity()));
		this.authProfileCache = authProfileCache;
		this.userSessionIndexService = userSessionIndexService;
		this.userSessionIndexSession = userSessionIndexSession;
		this.sessionLimitManager = sessionLimitManager;
		this.tokenService = tokenService;
		this.loginFailureRateLimiter = loginFailureRateLimiter;
		this.loginLogService = loginLogService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public UserLoginResponse login(AbstractAuthentication auth, HttpServletRequest request) {
		LoginStrategy<?> raw = lookupStrategyWithAudit(auth, request);

		LoginResult loginResult = ((LoginStrategy<AbstractAuthentication>) raw).authenticate(auth, request);
		AuthProfile profile = loginResult.authProfile();
		UserLoginResponse response = loginResult.response();
		Long userId = profile.getUserId();

		try {
			// 检查会话上限
			sessionLimitManager.assertSessionLimit(userId);

			// 创建缓存画像
			String jti = UUID.randomUUID().toString();
			long permVersion = profile.getPermVersion();

			authProfileCache.createCacheAuthProfile(profile);

			// 构建令牌
			TokenPair tokenPair = tokenService.buildTokenPair(userId, jti, permVersion);
			response.setAccessToken(tokenPair.accessToken());
			response.setRefreshToken(tokenPair.refreshToken());
			response.setExpires(tokenPair.accessExpiresAt());

			// 构建会话索引
			UserSessionIndex sessionIndex = userSessionIndexService.buildUserSessionIndex(request, userId, jti,
					response.getRefreshToken());
			userSessionIndexSession.createUserSessionIndex(sessionIndex);

			// 记录成功
			loginFailureRateLimiter.recordSuccess(userId);

			// 记录成功日志
			loginLogService.recordLoginLog(request, loginResult.loginLogType(), AuthLoginLogResult.SUCCESS, userId,
					response.getUsername(), null, jti);
			return response;
		}
		catch (AuthBusinessException ex) {
			// 记录失败日志
			loginLogService.recordLoginLog(request, loginResult.loginLogType(),
					LoginAuditSupport.resultForOrchestrationFailure(ex.getResultCode()), userId, response.getUsername(),
					ex.getMessageKey(), null);
			throw ex;
		}
	}

	/**
	 * 查找登录策略并记录审计日志。
	 * @param auth 认证请求
	 * @param request HTTP 请求
	 * @return 登录策略
	 */
	private LoginStrategy<?> lookupStrategyWithAudit(AbstractAuthentication auth, HttpServletRequest request) {
		try {
			return lookupStrategy(auth);
		}
		catch (AuthBusinessException ex) {
			loginLogService.recordLoginLog(request, LoginAuditSupport.eventTypeFromAuth(auth),
					AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR, null, LoginAuditSupport.principal(auth),
					ex.getMessageKey(), null);
			throw ex;
		}
	}

	/**
	 * 从注册表中查找匹配的策略（精确匹配 → 父类/接口匹配兜底）
	 * @param auth 认证请求
	 * @return 匹配的登录策略
	 */
	private LoginStrategy<?> lookupStrategy(AbstractAuthentication auth) {
		LoginStrategy<?> raw = strategyRegistry.get(auth.getClass());
		if (raw == null) {
			raw = strategyRegistry.entrySet()
				.stream()
				.filter(entry -> entry.getKey().isInstance(auth))
				.map(Map.Entry::getValue)
				.findFirst()
				.orElseThrow(() -> new AuthBusinessException(AuthResultCode.AUTH_TYPE_UNSUPPORTED));
		}
		return raw;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void logout(String accessToken, HttpServletRequest request) {
		try {
			tokenService.parseAccessTokenSafe(accessToken)
				.ifPresent(userToken -> revokeExternalAccessSession(request, userToken));
		}
		catch (Exception ex) {
			log.warn("Logout side-effect failed (session delete or audit): {}", ex.toString());
		}
	}

	/**
	 * 撤销外部访问令牌对应的服务端会话并记录登出审计。
	 * @param request HTTP 请求
	 * @param userToken 已校验含非空 userId 与 jti 的用户令牌载荷
	 */
	private void revokeExternalAccessSession(HttpServletRequest request, JwtUserToken userToken) {
		String jti = userToken.getJti();
		Long userId = userToken.getUserId();
		String username = SecurityUserUtils.getUsername();
		userSessionIndexSession.deleteSession(jti);
		userSessionIndexSession.removeActiveSession(userId, jti);
		loginLogService.recordLoginLog(request, AuthLoginLogType.LOGOUT, AuthLoginLogResult.SUCCESS, userId, username,
				null, jti);
	}

}
