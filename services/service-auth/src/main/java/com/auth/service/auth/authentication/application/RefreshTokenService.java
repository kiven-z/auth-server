package com.auth.service.auth.authentication.application;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.crypto.digest.MD5;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditCoordinator;
import com.auth.service.auth.authentication.domain.model.RefreshSessionResult;
import com.auth.service.auth.authentication.domain.model.TokenPair;
import com.auth.service.auth.authentication.infrastructure.token.TokenService;
import com.auth.service.auth.authorization.application.AuthProfileCacheApplicationService;
import com.auth.service.auth.session.infrastructure.redis.RedisUserSessionIndexRepository;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

/**
 * 刷新令牌应用服务：校验会话、旋转 Refresh Token、撤销会话。
 *
 * @author Bunny
 */
@Slf4j
@Service
public class RefreshTokenService {

	private final AuthProfileCacheApplicationService authProfileCacheApplicationService;

	private final RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	private final TokenService tokenService;

	private final JwtProperties jwtProperties;

	private final LoginAuditCoordinator loginAuditCoordinator;

	public RefreshTokenService(AuthProfileCacheApplicationService authProfileCacheApplicationService,
			RedisUserSessionIndexRepository redisUserSessionIndexRepository, TokenService tokenService,
			JwtProperties jwtProperties, LoginAuditCoordinator loginAuditCoordinator) {
		this.authProfileCacheApplicationService = authProfileCacheApplicationService;
		this.redisUserSessionIndexRepository = redisUserSessionIndexRepository;
		this.tokenService = tokenService;
		this.jwtProperties = jwtProperties;
		this.loginAuditCoordinator = loginAuditCoordinator;
	}

	/**
	 * 刷新令牌
	 * @param refreshToken 刷新令牌
	 * @param request 当前 HTTP 请求（审计 IP/UA）；允许为 null
	 * @return 刷新令牌结果（含 rememberMe，供 Cookie 策略使用）
	 */
	public RefreshSessionResult refreshToken(String refreshToken, HttpServletRequest request) {
		try {
			return refreshTokenInternal(refreshToken, request);
		}
		catch (AuthBusinessException ex) {
			Long userId = tryResolveUserIdFromRefreshToken(refreshToken);
			loginAuditCoordinator.auditRefreshTokenFailure(request, userId, ex);
			throw ex;
		}
	}

	/**
	 * 刷新令牌内部实现
	 * @param refreshToken 刷新令牌
	 * @param request HTTP 请求
	 * @return 刷新令牌响应
	 */
	private RefreshSessionResult refreshTokenInternal(String refreshToken, HttpServletRequest request) {
		// 刷新Token必须存在
		if (CharSequenceUtil.isBlank(refreshToken)) {
			throw new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_MISSING);
		}

		// 解析刷新Token是否合法、过期
		JwtUserToken refreshUserToken = tokenService.parseRefreshToken(refreshToken);
		Long refreshUserId = refreshUserToken.getUserId();
		String refreshJti = refreshUserToken.getJti();

		// 加载会话索引
		UserSessionIndex sessionIndex = redisUserSessionIndexRepository.loadUserSessionIndex(refreshJti)
			.orElseThrow(() -> new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_EXPIRED));
		assertRefreshTokenMatches(refreshUserId, refreshJti, sessionIndex, refreshToken);

		// 加载缓存画像
		AuthProfile profile = authProfileCacheApplicationService.loadCachedProfile(refreshUserId)
			.orElseThrow(() -> new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_EXPIRED));
		Long permVersion = Objects.requireNonNull(profile.getPermVersion(), "permVersion must not be null");

		TokenPair tokenPair = tokenService.buildTokenPair(refreshUserId, refreshJti, permVersion);

		// 旋转刷新令牌
		rotateRefreshInSession(refreshJti, tokenPair.refreshToken());

		boolean rememberMe = Boolean.TRUE.equals(sessionIndex.getRememberMe());

		loginAuditCoordinator.auditRefreshTokenSuccess(request, refreshUserId, refreshJti);
		return new RefreshSessionResult(tokenPair, rememberMe);
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
			JwtUserToken tokenResult = tokenService.parseRefreshToken(refreshToken);
			return tokenResult.getUserId();
		}
		catch (Exception ignored) {
			return null;
		}
	}

	/**
	 * 断言刷新令牌与会话中存储的哈希及过期时间一致。
	 * <p>
	 * 过期仅抛错；哈希为空或与请求令牌摘要不一致时视为脏会话，先撤销服务端会话再抛错（与文档强会话绑定一致）。
	 * </p>
	 * @param userId 令牌声明的用户 ID（用于活跃集合移除）
	 * @param jti 会话 ID
	 * @param sessionIndex 会话索引
	 * @param refreshToken 刷新令牌
	 */
	private void assertRefreshTokenMatches(Long userId, String jti, UserSessionIndex sessionIndex,
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
			redisUserSessionIndexRepository.deleteSession(jti);
			redisUserSessionIndexRepository.removeActiveSession(userId, jti);
			throw new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_EXPIRED);
		}

		// 刷新令牌与会话哈希不一致时撤销会话详情与活跃索引（幂等）
		boolean hashMatches = CharSequenceUtil.isNotBlank(redisHash) && Objects.equals(redisHash, requestHash);
		if (BooleanUtil.isFalse(hashMatches)) {
			redisUserSessionIndexRepository.deleteSession(jti);
			redisUserSessionIndexRepository.removeActiveSession(userId, jti);
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

		redisUserSessionIndexRepository.updateRefreshInfo(jti, refreshTokenHash, refreshTokenExpiresAt, ttl);
	}

	/**
	 * 按刷新令牌撤销服务端会话（登出等场景；令牌无效时静默跳过）。
	 * @param refreshToken 刷新令牌
	 * @param request 当前 HTTP 请求（审计）；允许为 null
	 */
	public void revokeByRefreshToken(String refreshToken, HttpServletRequest request) {
		if (CharSequenceUtil.isBlank(refreshToken)) {
			return;
		}
		try {
			JwtUserToken userToken = tokenService.parseRefreshToken(refreshToken);
			revokeServerSession(request, userToken);
		}
		catch (RuntimeException ex) {
			log.error("Failed to revoke session by refresh token", ex);
			throw ex;
		}
	}

	/**
	 * 删除刷新令牌对应的服务端会话并记录登出审计。
	 */
	private void revokeServerSession(HttpServletRequest request, JwtUserToken userToken) {
		String jti = userToken.getJti();
		Long userId = userToken.getUserId();
		redisUserSessionIndexRepository.deleteSession(jti);
		redisUserSessionIndexRepository.removeActiveSession(userId, jti);
		loginAuditCoordinator.auditLogoutOnRefreshTokenRevoke(request, userId, jti);
	}

}
