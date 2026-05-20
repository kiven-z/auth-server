package com.auth.service.auth.authentication.application;

import cn.hutool.core.util.BooleanUtil;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.authentication.api.dto.response.UserLoginResponse;
import com.auth.service.auth.authentication.application.assembler.LoginResponseAssembler;
import com.auth.service.auth.authentication.application.command.AbstractLoginCommand;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditCoordinator;
import com.auth.service.auth.authentication.application.port.LoginStrategy;
import com.auth.service.auth.authentication.domain.model.LoginResult;
import com.auth.service.auth.authentication.domain.model.TokenPair;
import com.auth.service.auth.authentication.infrastructure.cookie.RememberMeCookiePolicy;
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

	private final LoginAuditCoordinator loginAuditCoordinator;

	private final RememberMeCookiePolicy rememberMeCookiePolicy;

	public LoginApplicationService(List<LoginStrategy<?>> strategies,
			AuthProfileCacheApplicationService authProfileCacheApplicationService,
			UserSessionIndexFactory userSessionIndexFactory,
			RedisUserSessionIndexRepository redisUserSessionIndexRepository, SessionLimitManager sessionLimitManager,
			TokenService tokenService, LoginFailureRateLimiter loginFailureRateLimiter,
			LoginAuditCoordinator loginAuditCoordinator, RememberMeCookiePolicy rememberMeCookiePolicy) {
		this.strategyRegistry = strategies.stream()
			.collect(Collectors.toUnmodifiableMap(LoginStrategy::supports, Function.identity()));
		this.authProfileCacheApplicationService = authProfileCacheApplicationService;
		this.userSessionIndexFactory = userSessionIndexFactory;
		this.redisUserSessionIndexRepository = redisUserSessionIndexRepository;
		this.sessionLimitManager = sessionLimitManager;
		this.tokenService = tokenService;
		this.loginFailureRateLimiter = loginFailureRateLimiter;
		this.loginAuditCoordinator = loginAuditCoordinator;
		this.rememberMeCookiePolicy = rememberMeCookiePolicy;
	}

	/**
	 * 执行登录并返回已填充访问/刷新令牌的响应。
	 * @param command 登录命令
	 * @param request HTTP 请求
	 * @return 登录响应
	 */
	@SuppressWarnings("unchecked")
	public UserLoginResponse login(AbstractLoginCommand command, HttpServletRequest request) {
		// 查找登录策略并记录审计日志
		LoginStrategy<?> strategy = lookupStrategyWithAudit(command, request);

		// 执行登录并返回登录结果
		LoginResult loginResult = ((LoginStrategy<AbstractLoginCommand>) strategy).authenticate(command, request);

		// 获取用户画像
		AuthProfile profile = loginResult.authProfile();

		// 创建用户登录响应
		UserLoginResponse response = LoginResponseAssembler.toUserLoginResponse(loginResult);

		// 获取用户ID和用户名
		Long userId = profile.getUserId();
		String username = loginResult.authenticatedUser().username();
		boolean rememberMe = BooleanUtil.isTrue(command.getRememberMe());

		// 初始化变量
		String jti = null;
		boolean profileCached = false;
		boolean sessionCreated = false;
		try {
			// 校验会话限制
			sessionLimitManager.assertSessionLimit(userId);

			// 生成JTI
			jti = UUID.randomUUID().toString();

			// 获取权限版本
			long permVersion = profile.getPermVersion();

			// 缓存用户画像
			authProfileCacheApplicationService.cacheProfile(profile);
			profileCached = true;

			// 构建令牌对
			TokenPair tokenPair = tokenService.buildTokenPair(userId, jti, permVersion);
			response.setAccessToken(tokenPair.accessToken());
			response.setRefreshToken(tokenPair.refreshToken());
			response.setExpires(tokenPair.accessExpiresAt());

			// 构建用户会话索引
			UserSessionIndex sessionIndex = userSessionIndexFactory.buildUserSessionIndex(request, userId, jti,
					response.getRefreshToken(), rememberMe);
			redisUserSessionIndexRepository.createUserSessionIndex(sessionIndex);

			// 设置会话创建标志
			sessionCreated = true;
			response.setReadMeDay(rememberMeCookiePolicy.resolveReadMeDay());

			// 记录成功登录
			loginFailureRateLimiter.recordSuccess(userId);

			loginAuditCoordinator.auditLoginSuccess(request, loginResult.loginLogType(), userId, username, jti);
			return response;
		}
		catch (AuthBusinessException ex) {
			// 回滚登录痕迹
			rollbackLoginArtifacts(userId, jti, profileCached, sessionCreated);
			loginAuditCoordinator.auditOrchestrationFailure(request, loginResult.loginLogType(), userId, username, ex);
			throw ex;
		}
		catch (RuntimeException ex) {
			// 回滚登录痕迹
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
			// 获取登录策略
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
			loginAuditCoordinator.auditUnsupportedLoginCommand(request, command, ex.getMessageKey());
			throw ex;
		}
	}

	/**
	 * 登出当前会话（永远不抛出异常）。幂等：会话不存在/索引不存在都视为成功。
	 * @param accessToken Access Token（不含 Bearer 前缀）
	 * @param request HTTP 请求
	 */
	public void logout(String accessToken, HttpServletRequest request) {
		// 解析访问令牌
		tokenService.parseAccessTokenSafe(accessToken).ifPresent(userToken -> {
			// 获取JTI和用户ID
			String jti = userToken.getJti();
			Long userId = userToken.getUserId();
			String username = SecurityUserUtils.getUsername();

			// 删除会话索引
			redisUserSessionIndexRepository.deleteSession(jti);

			// 移除活跃会话
			redisUserSessionIndexRepository.removeActiveSession(userId, jti);

			// 记录成功登出
			loginAuditCoordinator.auditLogoutSuccess(request, userId, username, jti);
		});
	}

	/**
	 * 登录编排失败时回滚已写入的 Redis 状态，避免孤儿画像或会话。
	 */
	private void rollbackLoginArtifacts(Long userId, String jti, boolean profileCached, boolean sessionCreated) {
		// 如果会话已创建且JTI不为空，则删除会话索引
		if (sessionCreated && jti != null) {
			redisUserSessionIndexRepository.deleteSession(jti);
			redisUserSessionIndexRepository.removeActiveSession(userId, jti);
		}

		// 如果用户画像已缓存，则移除用户画像
		if (profileCached) {
			authProfileCacheApplicationService.evictProfile(userId);
		}
	}

}
