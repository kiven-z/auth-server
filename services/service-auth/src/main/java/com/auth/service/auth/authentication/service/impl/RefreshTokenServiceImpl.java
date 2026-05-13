package com.auth.service.auth.authentication.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.crypto.digest.MD5;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.authentication.audit.LoginAuditEventPublisher;
import com.auth.service.auth.authentication.profile.AuthProfileCache;
import com.auth.service.auth.authentication.service.RefreshTokenService;
import com.auth.service.auth.authentication.token.TokenPair;
import com.auth.service.auth.authentication.token.TokenService;
import com.auth.service.auth.session.infrastructure.RedisUserSessionIndexRepository;
import com.auth.service.auth.support.exception.AuthBusinessException;
import com.auth.service.auth.support.model.enums.AuthLoginLogResult;
import com.auth.service.auth.support.model.enums.AuthLoginLogType;
import com.auth.service.auth.support.model.enums.AuthResultCode;
import com.auth.service.auth.support.model.response.RefreshTokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

/**
 * 用户刷新Token
 *
 * @author Bunny
 */
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

	private final AuthProfileCache authProfileCache;

	private final RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	private final TokenService tokenService;

	private final JwtProperties jwtProperties;

	private final LoginAuditEventPublisher loginAuditEventPublisher;

	public RefreshTokenServiceImpl(AuthProfileCache authProfileCache,
			RedisUserSessionIndexRepository redisUserSessionIndexRepository, TokenService tokenService,
			JwtProperties jwtProperties, LoginAuditEventPublisher loginAuditEventPublisher) {
		this.authProfileCache = authProfileCache;
		this.redisUserSessionIndexRepository = redisUserSessionIndexRepository;
		this.tokenService = tokenService;
		this.jwtProperties = jwtProperties;
		this.loginAuditEventPublisher = loginAuditEventPublisher;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RefreshTokenResponse refreshToken(String refreshToken, HttpServletRequest request) {
		try {
			return refreshTokenInternal(refreshToken, request);
		}
		catch (AuthBusinessException ex) {
			Long userId = tryResolveUserIdFromRefreshToken(refreshToken);
			loginAuditEventPublisher.publish(request, AuthLoginLogType.REFRESH_TOKEN,
					AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR, userId, "/", ex.getMessageKey(), null);
			throw ex;
		}
	}

	/**
	 * 刷新令牌内部实现
	 * @param refreshToken 刷新令牌
	 * @param request HTTP 请求
	 * @return 刷新令牌响应
	 */
	private RefreshTokenResponse refreshTokenInternal(String refreshToken, HttpServletRequest request) {
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
		AuthProfile profile = authProfileCache.loadCacheAuthProfile(refreshUserId)
			.orElseThrow(() -> new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_EXPIRED));
		Long permVersion = Objects.requireNonNull(profile.getPermVersion(), "permVersion must not be null");

		TokenPair tokenPair = tokenService.buildTokenPair(refreshUserId, refreshJti, permVersion);

		// 旋转刷新令牌
		rotateRefreshInSession(refreshJti, tokenPair.refreshToken());

		// 构建刷新令牌响应
		RefreshTokenResponse response = new RefreshTokenResponse();
		response.setAccessToken(tokenPair.accessToken());
		response.setRefreshToken(tokenPair.refreshToken());
		response.setExpires(tokenPair.accessExpiresAt());

		loginAuditEventPublisher.publish(request, AuthLoginLogType.REFRESH_TOKEN, AuthLoginLogResult.SUCCESS,
				refreshUserId, null, null, refreshJti);
		return response;
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

}
