package com.auth.service.auth.authentication.application.impl;

import cn.hutool.core.util.BooleanUtil;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.authentication.api.dto.response.UserLoginResponse;
import com.auth.service.auth.authentication.application.AuthService;
import com.auth.service.auth.authentication.application.command.AbstractLoginCommand;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogResult;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.event.LoginAuditEventPublisher;
import com.auth.service.auth.authentication.domain.model.LoginResult;
import com.auth.service.auth.authentication.domain.model.TokenPair;
import com.auth.service.auth.authentication.domain.port.LoginStrategy;
import com.auth.service.auth.authentication.infrastructure.audit.LoginAuditContextResolver;
import com.auth.service.auth.authentication.infrastructure.support.AuthTokenResponseEnricher;
import com.auth.service.auth.authentication.infrastructure.token.LoginFailureRateLimiter;
import com.auth.service.auth.authentication.infrastructure.token.TokenService;
import com.auth.service.auth.authorization.infrastructure.cache.AuthProfileCache;
import com.auth.service.auth.session.infrastructure.redis.RedisUserSessionIndexRepository;
import com.auth.service.auth.session.infrastructure.redis.support.SessionLimitManager;
import com.auth.service.auth.session.infrastructure.redis.support.UserSessionIndexFactory;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
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

	private final Map<Class<? extends AbstractLoginCommand>, LoginStrategy<?>> strategyRegistry;

	private final AuthProfileCache authProfileCache;

	private final UserSessionIndexFactory userSessionIndexFactory;

	private final RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	private final SessionLimitManager sessionLimitManager;

	private final TokenService tokenService;

	private final LoginFailureRateLimiter loginFailureRateLimiter;

	private final LoginAuditEventPublisher loginAuditEventPublisher;

	private final AuthTokenResponseEnricher authTokenResponseEnricher;

	public AuthServiceImpl(List<LoginStrategy<?>> strategies, AuthProfileCache authProfileCache,
			UserSessionIndexFactory userSessionIndexFactory,
			RedisUserSessionIndexRepository redisUserSessionIndexRepository, SessionLimitManager sessionLimitManager,
			TokenService tokenService, LoginFailureRateLimiter loginFailureRateLimiter,
			LoginAuditEventPublisher loginAuditEventPublisher, AuthTokenResponseEnricher authTokenResponseEnricher) {
		this.strategyRegistry = strategies.stream()
			.collect(Collectors.toUnmodifiableMap(LoginStrategy::supports, Function.identity()));
		this.authProfileCache = authProfileCache;
		this.userSessionIndexFactory = userSessionIndexFactory;
		this.redisUserSessionIndexRepository = redisUserSessionIndexRepository;
		this.sessionLimitManager = sessionLimitManager;
		this.tokenService = tokenService;
		this.loginFailureRateLimiter = loginFailureRateLimiter;
		this.loginAuditEventPublisher = loginAuditEventPublisher;
		this.authTokenResponseEnricher = authTokenResponseEnricher;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public UserLoginResponse login(AbstractLoginCommand command, HttpServletRequest request) {
		LoginStrategy<?> strategy = lookupStrategyWithAudit(command, request);

		LoginResult loginResult = ((LoginStrategy<AbstractLoginCommand>) strategy).authenticate(command, request);
		AuthProfile profile = loginResult.authProfile();
		UserLoginResponse response = loginResult.response();
		Long userId = profile.getUserId();
		boolean rememberMe = BooleanUtil.isTrue(command.getRememberMe());

		String jti = null;
		boolean profileCached = false;
		boolean sessionCreated = false;
		try {
			// 检查会话上限
			sessionLimitManager.assertSessionLimit(userId);

			jti = UUID.randomUUID().toString();
			long permVersion = profile.getPermVersion();

			authProfileCache.createCacheAuthProfile(profile);
			profileCached = true;

			// 构建令牌
			TokenPair tokenPair = tokenService.buildTokenPair(userId, jti, permVersion);
			response.setAccessToken(tokenPair.accessToken());
			response.setRefreshToken(tokenPair.refreshToken());
			response.setExpires(tokenPair.accessExpiresAt());

			// 构建会话索引
			UserSessionIndex sessionIndex = userSessionIndexFactory.buildUserSessionIndex(request, userId, jti,
					response.getRefreshToken(), rememberMe);
			redisUserSessionIndexRepository.createUserSessionIndex(sessionIndex);
			sessionCreated = true;
			authTokenResponseEnricher.enrichReadMeDay(response);

			// 记录成功
			loginFailureRateLimiter.recordSuccess(userId);

			// 记录成功日志
			loginAuditEventPublisher.publish(request, loginResult.loginLogType(), AuthLoginLogResult.SUCCESS, userId,
					response.getUsername(), null, jti);
			return response;
		}
		catch (AuthBusinessException ex) {
			rollbackLoginArtifacts(userId, jti, profileCached, sessionCreated);
			loginAuditEventPublisher.publish(request, loginResult.loginLogType(),
					LoginAuditContextResolver.resultForOrchestrationFailure(ex.getResultCode()), userId,
					response.getUsername(), ex.getMessageKey(), null);
			throw ex;
		}
		catch (RuntimeException ex) {
			rollbackLoginArtifacts(userId, jti, profileCached, sessionCreated);
			log.error("Login orchestration failed for userId={}", userId, ex);
			throw ex;
		}
	}

	/**
	 * 查找登录策略并记录审计日志。
	 * @param command 认证请求
	 * @param request HTTP 请求
	 * @return 登录策略
	 */
	private LoginStrategy<?> lookupStrategyWithAudit(AbstractLoginCommand command, HttpServletRequest request) {
		try {
			// 从注册表中查找匹配的策略（精确匹配 → 父类/接口匹配兜底）
			LoginStrategy<?> raw = strategyRegistry.get(command.getClass());
			if (raw == null) {
				raw = strategyRegistry.entrySet()
					.stream()
					.filter(entry -> entry.getKey().isInstance(command))
					.map(Map.Entry::getValue)
					.findFirst()
					.orElseThrow(() -> new AuthBusinessException(AuthResultCode.AUTH_TYPE_UNSUPPORTED));
			}
			return raw;
		}
		catch (AuthBusinessException ex) {
			loginAuditEventPublisher.publish(request, command.loginLogType(),
					AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR, null, command.principalForAudit(),
					ex.getMessageKey(), null);
			throw ex;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void logout(String accessToken, HttpServletRequest request) {
		// 撤销外部访问令牌对应的服务端会话并记录登出审计
		tokenService.parseAccessTokenSafe(accessToken).ifPresent(userToken -> {
			String jti = userToken.getJti();
			Long userId = userToken.getUserId();
			String username = SecurityUserUtils.getUsername();
			redisUserSessionIndexRepository.deleteSession(jti);
			redisUserSessionIndexRepository.removeActiveSession(userId, jti);
			loginAuditEventPublisher.publish(request, AuthLoginLogType.LOGOUT, AuthLoginLogResult.SUCCESS, userId,
					username, null, jti);
		});
	}

	/**
	 * 登录编排失败时回滚已写入的 Redis 状态，避免孤儿画像或会话。
	 */
	private void rollbackLoginArtifacts(Long userId, String jti, boolean profileCached, boolean sessionCreated) {
		if (sessionCreated && jti != null) {
			redisUserSessionIndexRepository.deleteSession(jti);
			redisUserSessionIndexRepository.removeActiveSession(userId, jti);
		}
		if (profileCached) {
			authProfileCache.evictCacheAuthProfile(userId);
		}
	}

}
