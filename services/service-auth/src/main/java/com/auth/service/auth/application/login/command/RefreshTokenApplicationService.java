package com.auth.service.auth.application.login.command;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.MD5;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.application.login.LoginAuditService;
import com.auth.service.auth.application.port.login.AuthProfileCachePort;
import com.auth.service.auth.application.port.login.TokenPort;
import com.auth.service.auth.application.port.session.SessionPort;
import com.auth.service.auth.domain.authentication.model.LogoutSessionHint;
import com.auth.service.auth.domain.authentication.model.RefreshSessionResult;
import com.auth.service.auth.domain.authentication.model.TokenPair;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.AuthResultCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * 刷新令牌应用服务：校验会话、旋转 Refresh Token、撤销会话
 *
 * @author Bunny
 */
@Slf4j
@Service
public class RefreshTokenApplicationService {

	private final AuthProfileCachePort authProfileCachePort;

	private final SessionPort sessionGateway;

	private final TokenPort tokenGateway;

	private final JwtProperties jwtProperties;

	private final LoginAuditService loginAuditService;

	public RefreshTokenApplicationService(AuthProfileCachePort authProfileCachePort, SessionPort sessionGateway,
			TokenPort tokenGateway, JwtProperties jwtProperties, LoginAuditService loginAuditService) {
		this.authProfileCachePort = authProfileCachePort;
		this.sessionGateway = sessionGateway;
		this.tokenGateway = tokenGateway;
		this.jwtProperties = jwtProperties;
		this.loginAuditService = loginAuditService;
	}

	/**
	 * 刷新令牌
	 * @param refreshToken 刷新令牌
	 * @param request 当前 HTTP 请求（审计 IP/UA）；允许为 null
	 * @return 刷新令牌结果（含 rememberMe，供 Cookie 策略使用）
	 */
	public RefreshSessionResult refreshToken(String refreshToken, HttpServletRequest request) {
		try {
			// 如果刷新令牌为空，则抛出异常
			if (CharSequenceUtil.isBlank(refreshToken)) {
				throw new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_MISSING);
			}

			// 解析刷新令牌
			JwtUserToken refreshUserToken = tokenGateway.parseRefreshToken(refreshToken);
			// 获取用户ID
			Long refreshUserId = refreshUserToken.getUserId();
			// 获取会话ID
			String refreshJti = refreshUserToken.getJti();

			// 加载用户会话索引
			UserSessionIndex sessionIndex = sessionGateway.loadUserSessionIndex(refreshJti)
				.orElseThrow(() -> new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_EXPIRED));
			// 断言刷新令牌与会话中存储的哈希及过期时间一致
			assertRefreshTokenMatches(refreshUserId, refreshJti, sessionIndex, refreshToken);

			// 加载用户画像
			AuthProfile profile = authProfileCachePort.loadCachedProfile(refreshUserId)
				.orElseThrow(() -> new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_EXPIRED));
			// 获取权限版本
			Long permVersion = Objects.requireNonNull(profile.getPermVersion(), "permVersion must not be null");

			// 旋转刷新令牌
			TokenPair tokenPair = tokenGateway.buildTokenPair(refreshUserId, refreshJti, permVersion);
			// 旋转刷新令牌
			rotateRefreshInSession(refreshJti, tokenPair.refreshToken());

			// 获取是否记住我
			boolean rememberMe = BooleanUtil.isTrue(sessionIndex.getRememberMe());
			// 记录成功刷新令牌
			loginAuditService.auditRefreshTokenSuccess(request, refreshUserId, refreshJti);
			// 返回刷新令牌结果
			return new RefreshSessionResult(tokenPair, rememberMe);
		}
		catch (AuthBusinessException ex) {
			Long userId = tryResolveUserIdFromRefreshToken(refreshToken);
			loginAuditService.auditRefreshTokenFailure(request, userId, ex);
			throw ex;
		}
	}

	/**
	 * 尝试从刷新令牌中解析用户ID
	 * @param refreshToken 刷新令牌
	 * @return 用户ID
	 */
	private Long tryResolveUserIdFromRefreshToken(String refreshToken) {
		if (CharSequenceUtil.isBlank(refreshToken)) {
			return null;
		}
		try {
			JwtUserToken tokenResult = tokenGateway.parseRefreshToken(refreshToken);
			return tokenResult.getUserId();
		}
		catch (Exception ignored) {
			return null;
		}
	}

	/**
	 * 断言刷新令牌与会话中存储的哈希及过期时间一致
	 * <p>
	 * 过期仅抛错；哈希为空或与请求令牌摘要不一致时视为脏会话，先撤销服务端会话再抛错
	 * </p>
	 * @param userId 令牌声明的用户 ID（用于活跃集合移除）
	 * @param jti 会话 ID
	 * @param sessionIndex 会话索引
	 * @param refreshToken 刷新令牌
	 */
	private void assertRefreshTokenMatches(Long userId, String jti, @NotNull UserSessionIndex sessionIndex,
			String refreshToken) {
		Long expiresAt = sessionIndex.getRefreshTokenExpiresAt();
		Long sessionUserId = sessionIndex.getUserId();
		String redisHash = sessionIndex.getRefreshTokenHash();
		String requestHash = MD5.create().digestHex16(refreshToken);
		long now = System.currentTimeMillis();

		if (expiresAt == null || expiresAt <= now) {
			throw new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_EXPIRED);
		}
		if (BooleanUtil.isFalse(Objects.equals(userId, sessionUserId))) {
			long indexUserId = ObjectUtil.defaultIfNull(sessionUserId, userId);
			sessionGateway.terminateSession(indexUserId, jti);
			throw new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_EXPIRED);
		}

		boolean hashMatches = CharSequenceUtil.isNotBlank(redisHash) && Objects.equals(redisHash, requestHash);
		if (BooleanUtil.isFalse(hashMatches)) {
			sessionGateway.terminateSession(userId, jti);
			throw new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_EXPIRED);
		}
	}

	/**
	 * 旋转刷新令牌
	 * @param jti 会话ID
	 * @param refreshToken 刷新令牌
	 */
	private void rotateRefreshInSession(String jti, String refreshToken) {
		String refreshTokenHash = MD5.create().digestHex16(refreshToken);
		long refreshExpiredSeconds = jwtProperties.getRefreshExpired();
		long refreshTokenExpiresAt = System.currentTimeMillis() + refreshExpiredSeconds * 1000L;
		Duration ttl = Duration.ofSeconds(refreshExpiredSeconds);

		sessionGateway.updateRefreshInfo(jti, refreshTokenHash, refreshTokenExpiresAt, ttl);
	}

	/**
	 * 按刷新令牌撤销服务端会话（登出等场景；令牌无效时静默跳过）
	 * @param refreshToken 刷新令牌
	 */
	public Optional<LogoutSessionHint> revokeByRefreshToken(String refreshToken) {
		if (CharSequenceUtil.isBlank(refreshToken)) {
			return Optional.empty();
		}
		try {
			JwtUserToken userToken = tokenGateway.parseRefreshToken(refreshToken);
			return Optional.of(revokeServerSession(userToken));
		}
		catch (RuntimeException ex) {
			log.error("Failed to revoke session by refresh token", ex);
			throw ex;
		}
	}

	/**
	 * 删除刷新令牌对应的服务端会话
	 * @param userToken 刷新令牌解析结果
	 * @return 登出审计上下文
	 */
	private LogoutSessionHint revokeServerSession(JwtUserToken userToken) {
		String jti = userToken.getJti();
		Long userId = userToken.getUserId();
		sessionGateway.terminateSession(userId, jti);
		return new LogoutSessionHint(userId, jti);
	}

}
