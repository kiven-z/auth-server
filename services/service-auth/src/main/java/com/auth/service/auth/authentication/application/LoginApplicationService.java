package com.auth.service.auth.authentication.application;

import cn.hutool.core.util.BooleanUtil;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.authentication.api.dto.response.UserLoginResponse;
import com.auth.service.auth.authentication.application.assembler.LoginResponseAssembler;
import com.auth.service.auth.authentication.application.command.AbstractLoginCommand;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditCoordinator;
import com.auth.service.auth.authentication.application.port.*;
import com.auth.service.auth.authentication.domain.model.LoginResult;
import com.auth.service.auth.authentication.domain.model.TokenPair;
import com.auth.service.auth.authentication.domain.port.LoginFailureRateLimitPort;
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

	private final AuthProfileCachePort authProfileCachePort;

	private final UserSessionIndexBuildPort userSessionIndexBuildPort;

	private final UserSessionIndexPort userSessionIndexPort;

	private final SessionLimitPort sessionLimitPort;

	private final TokenIssuerPort tokenIssuerPort;

	private final LoginFailureRateLimitPort loginFailureRateLimitPort;

	private final LoginAuditCoordinator loginAuditCoordinator;

	private final RememberMePolicyPort rememberMePolicyPort;

	public LoginApplicationService(List<LoginStrategy<?>> strategies, AuthProfileCachePort authProfileCachePort,
			UserSessionIndexBuildPort userSessionIndexBuildPort, UserSessionIndexPort userSessionIndexPort,
			SessionLimitPort sessionLimitPort, TokenIssuerPort tokenIssuerPort,
			LoginFailureRateLimitPort loginFailureRateLimitPort, LoginAuditCoordinator loginAuditCoordinator,
			RememberMePolicyPort rememberMePolicyPort) {
		this.strategyRegistry = strategies.stream()
			.collect(Collectors.toUnmodifiableMap(LoginStrategy::supports, Function.identity()));
		this.authProfileCachePort = authProfileCachePort;
		this.userSessionIndexBuildPort = userSessionIndexBuildPort;
		this.userSessionIndexPort = userSessionIndexPort;
		this.sessionLimitPort = sessionLimitPort;
		this.tokenIssuerPort = tokenIssuerPort;
		this.loginFailureRateLimitPort = loginFailureRateLimitPort;
		this.loginAuditCoordinator = loginAuditCoordinator;
		this.rememberMePolicyPort = rememberMePolicyPort;
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
			sessionLimitPort.assertSessionLimit(userId);

			// 生成JTI
			jti = UUID.randomUUID().toString();

			// 获取权限版本
			long permVersion = profile.getPermVersion();

			// 缓存用户画像
			authProfileCachePort.cacheProfile(profile);
			profileCached = true;

			// 构建令牌对
			TokenPair tokenPair = tokenIssuerPort.buildTokenPair(userId, jti, permVersion);
			response.setAccessToken(tokenPair.accessToken());
			response.setRefreshToken(tokenPair.refreshToken());
			response.setExpires(tokenPair.accessExpiresAt());

			// 构建用户会话索引
			UserSessionIndex sessionIndex = userSessionIndexBuildPort.build(request, userId, jti,
					response.getRefreshToken(), rememberMe);
			userSessionIndexPort.createUserSessionIndex(sessionIndex);

			// 设置会话创建标志
			sessionCreated = true;
			response.setReadMeDay(rememberMePolicyPort.resolveReadMeDay());

			loginFailureRateLimitPort.recordSuccess(userId);
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
		tokenIssuerPort.parseAccessTokenSafe(accessToken).ifPresent(userToken -> {
			String jti = userToken.getJti();
			Long userId = userToken.getUserId();
			String username = SecurityUserUtils.getUsername();

			// 获取JTI和用户ID
			userSessionIndexPort.deleteSession(jti);
			userSessionIndexPort.removeActiveSession(userId, jti);

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
			userSessionIndexPort.deleteSession(jti);
			userSessionIndexPort.removeActiveSession(userId, jti);
		}

		// 如果用户画像已缓存，则移除用户画像
		if (profileCached) {
			authProfileCachePort.evictProfile(userId);
		}
	}

}
