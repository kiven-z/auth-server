package com.auth.service.auth.authentication.application;

import cn.hutool.core.util.BooleanUtil;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.authentication.api.dto.response.UserLoginResponse;
import com.auth.service.auth.authentication.application.assembler.LoginResponseAssembler;
import com.auth.service.auth.authentication.application.command.AbstractLoginCommand;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogResult;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.event.LoginAuditEventPublisher;
import com.auth.service.auth.authentication.domain.model.LoginResult;
import com.auth.service.auth.authentication.domain.model.TokenPair;
import com.auth.service.auth.authentication.domain.port.LoginStrategy;
import com.auth.service.auth.authentication.application.support.LoginAuditContextResolver;
import com.auth.service.auth.authentication.infrastructure.support.AuthTokenResponseEnricher;
import com.auth.service.auth.authentication.infrastructure.token.LoginFailureRateLimiter;
import com.auth.service.auth.authentication.infrastructure.token.TokenService;
import com.auth.service.auth.authorization.application.AuthProfileCacheApplicationService;
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
 * 登录应用服务：凭证校验后的令牌签发、会话与画像缓存编排。
 *
 * @author Bunny
 */
@Slf4j
@Service
public class LoginApplicationService {

	private final Map<Class<? extends AbstractLoginCommand>, LoginStrategy<?>> strategyRegistry;

	private final AuthProfileCacheApplicationService authProfileCacheApplicationService;

	private final UserSessionIndexFactory userSessionIndexFactory;

	private final RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	private final SessionLimitManager sessionLimitManager;

	private final TokenService tokenService;

	private final LoginFailureRateLimiter loginFailureRateLimiter;

	private final LoginAuditEventPublisher loginAuditEventPublisher;

	private final AuthTokenResponseEnricher authTokenResponseEnricher;

	public LoginApplicationService(List<LoginStrategy<?>> strategies,
			AuthProfileCacheApplicationService authProfileCacheApplicationService,
			UserSessionIndexFactory userSessionIndexFactory,
			RedisUserSessionIndexRepository redisUserSessionIndexRepository, SessionLimitManager sessionLimitManager,
			TokenService tokenService, LoginFailureRateLimiter loginFailureRateLimiter,
			LoginAuditEventPublisher loginAuditEventPublisher, AuthTokenResponseEnricher authTokenResponseEnricher) {
		this.strategyRegistry = strategies.stream()
			.collect(Collectors.toUnmodifiableMap(LoginStrategy::supports, Function.identity()));
		this.authProfileCacheApplicationService = authProfileCacheApplicationService;
		this.userSessionIndexFactory = userSessionIndexFactory;
		this.redisUserSessionIndexRepository = redisUserSessionIndexRepository;
		this.sessionLimitManager = sessionLimitManager;
		this.tokenService = tokenService;
		this.loginFailureRateLimiter = loginFailureRateLimiter;
		this.loginAuditEventPublisher = loginAuditEventPublisher;
		this.authTokenResponseEnricher = authTokenResponseEnricher;
	}

	/**
	 * 执行登录并返回已填充访问/刷新令牌的响应。
	 * @param command 登录命令
	 * @param request HTTP 请求
	 * @return 登录响应
	 */
	@SuppressWarnings("unchecked")
	public UserLoginResponse login(AbstractLoginCommand command, HttpServletRequest request) {
		LoginStrategy<?> strategy = lookupStrategyWithAudit(command, request);

		LoginResult loginResult = ((LoginStrategy<AbstractLoginCommand>) strategy).authenticate(command, request);
		AuthProfile profile = loginResult.authProfile();
		UserLoginResponse response = LoginResponseAssembler.toUserLoginResponse(loginResult);
		Long userId = profile.getUserId();
		String username = loginResult.authenticatedUser().username();
		boolean rememberMe = BooleanUtil.isTrue(command.getRememberMe());

		String jti = null;
		boolean profileCached = false;
		boolean sessionCreated = false;
		try {
			sessionLimitManager.assertSessionLimit(userId);

			jti = UUID.randomUUID().toString();
			long permVersion = profile.getPermVersion();

			authProfileCacheApplicationService.cacheProfile(profile);
			profileCached = true;

			TokenPair tokenPair = tokenService.buildTokenPair(userId, jti, permVersion);
			response.setAccessToken(tokenPair.accessToken());
			response.setRefreshToken(tokenPair.refreshToken());
			response.setExpires(tokenPair.accessExpiresAt());

			UserSessionIndex sessionIndex = userSessionIndexFactory.buildUserSessionIndex(request, userId, jti,
					response.getRefreshToken(), rememberMe);
			redisUserSessionIndexRepository.createUserSessionIndex(sessionIndex);
			sessionCreated = true;
			authTokenResponseEnricher.enrichReadMeDay(response);

			loginFailureRateLimiter.recordSuccess(userId);

			loginAuditEventPublisher.publish(request, loginResult.loginLogType(), AuthLoginLogResult.SUCCESS, userId,
					username, null, jti);
			return response;
		}
		catch (AuthBusinessException ex) {
			rollbackLoginArtifacts(userId, jti, profileCached, sessionCreated);
			loginAuditEventPublisher.publish(request, loginResult.loginLogType(),
					LoginAuditContextResolver.resultForOrchestrationFailure(ex.getResultCode()), userId, username,
					ex.getMessageKey(), null);
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
	 * 登出当前会话（永远不抛出异常）。幂等：会话不存在/索引不存在都视为成功。
	 * @param accessToken Access Token（不含 Bearer 前缀）
	 * @param request HTTP 请求
	 */
	public void logout(String accessToken, HttpServletRequest request) {
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
			authProfileCacheApplicationService.evictProfile(userId);
		}
	}

}
