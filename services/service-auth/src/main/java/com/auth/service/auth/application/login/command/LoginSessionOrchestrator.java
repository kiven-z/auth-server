package com.auth.service.auth.application.login.command;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.application.login.LoginAuditService;
import com.auth.service.auth.application.port.login.AuthProfileCachePort;
import com.auth.service.auth.application.port.login.CredentialPort;
import com.auth.service.auth.application.port.login.RefreshCookiePort;
import com.auth.service.auth.application.port.login.TokenPort;
import com.auth.service.auth.application.port.session.SessionPort;
import com.auth.service.auth.domain.authentication.model.AuthenticatedUser;
import com.auth.service.auth.domain.authentication.model.CompletedLoginSession;
import com.auth.service.auth.domain.authentication.model.LoginResult;
import com.auth.service.auth.domain.authentication.model.TokenPair;
import com.auth.service.auth.domain.authentication.model.enums.AuthLoginLogType;
import com.auth.service.auth.shared.exception.AuthBusinessException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

	private final SessionPort sessionGateway;

	private final AuthProfileCachePort authProfileCachePort;

	private final TokenPort tokenGateway;

	private final CredentialPort credentialGateway;

	private final LoginAuditService loginAuditService;

	private final RefreshCookiePort refreshCookiePort;

	/**
	 * 在凭证已校验通过后签发令牌并建立服务端会话。
	 * @param loginResult 策略认证结果（含画像与审计类型）
	 * @param rememberMe 是否记住我
	 * @param request HTTP 请求（会话索引与审计）
	 * @return 已填充访问/刷新令牌及 readMeDay 的登录会话读模型
	 */
	public CompletedLoginSession issueSession(LoginResult loginResult, boolean rememberMe, HttpServletRequest request) {
		AuthProfile profile = loginResult.authProfile();
		Long userId = profile.getUserId();
		AuthenticatedUser user = loginResult.authenticatedUser();
		String username = user.username();
		AuthLoginLogType loginLogType = loginResult.loginLogType();

		CompletedLoginSession session = new CompletedLoginSession();
		session.setId(user.id());
		session.setUsername(user.username());
		session.setNickname(user.nickname());
		session.setAvatar(user.avatar());
		session.setRoles(profile.getRoles());
		session.setPermissions(profile.getPermissions());

		String jti = null;
		boolean profileCached = false;
		boolean sessionCreated = false;
		try {
			sessionGateway.assertSessionLimit(userId);

			jti = UUID.randomUUID().toString();
			long permVersion = profile.getPermVersion();

			authProfileCachePort.cacheProfile(profile);
			profileCached = true;

			TokenPair tokenPair = tokenGateway.buildTokenPair(userId, jti, permVersion);
			session.setAccessToken(tokenPair.accessToken());
			session.setRefreshToken(tokenPair.refreshToken());
			session.setExpires(tokenPair.accessExpiresAt());

			UserSessionIndex sessionIndex = sessionGateway.buildSessionIndex(request, userId, jti,
					session.getRefreshToken(), rememberMe);
			sessionGateway.registerSession(sessionIndex);
			sessionCreated = true;

			session.setReadMeDay(refreshCookiePort.resolveReadMeDay());

			credentialGateway.recordSuccess(userId);
			loginAuditService.auditLoginSuccess(request, loginLogType, userId, username, jti);
			return session;
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
	 * 会话签发失败时回滚已写入的 Redis 状态，避免孤儿画像或会话
	 * @param userId 用户ID
	 * @param jti 令牌ID
	 * @param profileCached 画像是否已缓存
	 * @param sessionCreated 会话是否已创建
	 */
	private void rollbackLoginArtifacts(Long userId, String jti, boolean profileCached, boolean sessionCreated) {
		if (sessionCreated && jti != null) {
			sessionGateway.terminateSession(userId, jti);
		}
		if (profileCached) {
			authProfileCachePort.evictProfile(userId);
		}
	}

}
