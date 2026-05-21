package com.auth.service.auth.authentication.application.session;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.authentication.api.dto.response.UserLoginResponse;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditService;
import com.auth.service.auth.authentication.application.port.*;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.model.LoginResult;
import com.auth.service.auth.authentication.domain.model.TokenPair;
import com.auth.service.auth.authentication.domain.port.LoginFailureRateLimitPort;
import com.auth.service.auth.authentication.domain.port.SessionLimitPort;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 登录会话签发编排：凭证校验通过后，统一完成会话上限检查、画像缓存、令牌签发、Redis 会话建立与成功审计。
 * <p>
 * 供密码/邮箱/短信登录及后续第三方登录、MFA 通过后复用，避免在 {@code LoginApplicationService} 中重复编排。
 * </p>
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class LoginSessionOrchestrator {

	private final SessionLimitPort sessionLimitPort;

	private final AuthProfileCachePort authProfileCachePort;

	private final TokenIssuerPort tokenIssuerPort;

	private final UserSessionIndexBuildPort userSessionIndexBuildPort;

	private final UserSessionIndexPort userSessionIndexPort;

	private final LoginFailureRateLimitPort loginFailureRateLimitPort;

	private final LoginAuditService loginAuditService;

	private final RememberMePolicyPort rememberMePolicyPort;

	/**
	 * 在凭证已校验通过后签发令牌并建立服务端会话。
	 * @param loginResult 策略认证结果（含画像与审计类型）
	 * @param response 已组装的登录响应（尚未填充令牌）
	 * @param rememberMe 是否记住我
	 * @param request HTTP 请求（会话索引与审计）
	 * @return 已填充访问/刷新令牌及 readMeDay 的响应
	 */
	public UserLoginResponse issueSession(LoginResult loginResult, UserLoginResponse response, boolean rememberMe,
			HttpServletRequest request) {
		// 获取认证用户画像
		AuthProfile profile = loginResult.authProfile();
		Long userId = profile.getUserId();
		String username = loginResult.authenticatedUser().username();

		// 获取登录日志类型
		AuthLoginLogType loginLogType = loginResult.loginLogType();

		// 初始化会话 ID
		String jti = null;
		boolean profileCached = false;
		boolean sessionCreated = false;
		try {
			// 校验用户会话上限
			sessionLimitPort.assertSessionLimit(userId);

			// 生成会话 ID
			jti = UUID.randomUUID().toString();
			// 获取权限版本
			long permVersion = profile.getPermVersion();

			// 缓存用户画像
			authProfileCachePort.cacheProfile(profile);
			// 标记用户画像已缓存
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
			// 标记会话已创建
			sessionCreated = true;

			// 设置记住我天数
			response.setReadMeDay(rememberMePolicyPort.resolveReadMeDay());

			// 记录登录成功
			loginFailureRateLimitPort.recordSuccess(userId);
			loginAuditService.auditLoginSuccess(request, loginLogType, userId, username, jti);
			return response;
		}
		catch (AuthBusinessException ex) {
			rollbackLoginArtifacts(userId, jti, profileCached, sessionCreated);
			loginAuditService.auditOrchestrationFailure(request, loginLogType, userId, username, ex);
			throw ex;
		}
		catch (RuntimeException ex) {
			rollbackLoginArtifacts(userId, jti, profileCached, sessionCreated);
			log.error("Login session issuance failed for userId={}", userId, ex);
			throw ex;
		}
	}

	/**
	 * 会话签发失败时回滚已写入的 Redis 状态，避免孤儿画像或会话。
	 */
	private void rollbackLoginArtifacts(Long userId, String jti, boolean profileCached, boolean sessionCreated) {
		if (sessionCreated && jti != null) {
			userSessionIndexPort.deleteSession(jti);
			userSessionIndexPort.removeActiveSession(userId, jti);
		}
		if (profileCached) {
			authProfileCachePort.evictProfile(userId);
		}
	}

}
