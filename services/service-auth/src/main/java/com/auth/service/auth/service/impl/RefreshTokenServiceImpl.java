package com.auth.service.auth.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.crypto.digest.MD5;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.api.SecurityTokenKind;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.infrastructure.login.TokenService;
import com.auth.service.auth.infrastructure.profile.AuthProfileCache;
import com.auth.service.auth.infrastructure.session.UserSessionIndexSession;
import com.auth.service.auth.model.enums.AuthLoginLogResult;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.response.RefreshTokenResponse;
import com.auth.service.auth.model.response.UserLoginResponse;
import com.auth.service.auth.service.AuthLoginAuditService;
import com.auth.service.auth.service.RefreshTokenService;
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

	private final UserSessionIndexSession userSessionIndexSession;

	private final TokenService tokenService;

	private final JwtProperties jwtProperties;

	private final AuthLoginAuditService loginLogService;

	public RefreshTokenServiceImpl(AuthProfileCache authProfileCache, UserSessionIndexSession userSessionIndexSession,
			TokenService tokenService, JwtProperties jwtProperties, AuthLoginAuditService loginLogService) {
		this.authProfileCache = authProfileCache;
		this.userSessionIndexSession = userSessionIndexSession;
		this.tokenService = tokenService;
		this.jwtProperties = jwtProperties;
		this.loginLogService = loginLogService;
	}

	/**
	 * 刷新令牌
	 * @param refreshToken 刷新令牌
	 * @param request HTTP 请求
	 * @return 刷新令牌响应
	 */
	@Override
	public RefreshTokenResponse refreshToken(String refreshToken, HttpServletRequest request) {
		try {
			return refreshTokenInternal(refreshToken, request);
		}
		catch (AuthBusinessException ex) {
			Long userId = tryResolveUserIdFromRefreshToken(refreshToken);
			loginLogService.recordLoginLog(request, AuthLoginLogType.REFRESH_TOKEN,
					AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR, userId, null, ex.getMessageKey(), null);
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
		// 解析并断言外部刷新令牌
		SecurityTokenResult tokenResult = parseAndAssertExternalRefresh(refreshToken);
		JwtUserToken userToken = tokenResult.getUserToken();
		Long userId = Objects.requireNonNull(userToken.getUserId(), "userId must not be null");
		String jti = userToken.getJti();

		// 加载会话索引
		UserSessionIndex sessionIndex = userSessionIndexSession.loadUserSessionIndex(jti)
			.orElseThrow(() -> new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_EXPIRED));
		assertRefreshTokenMatches(userId, jti, sessionIndex, refreshToken);

		// 加载缓存画像
		AuthProfile profile = authProfileCache.loadCacheAuthProfile(userId)
			.orElseThrow(() -> new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_EXPIRED));
		Long permVersion = Objects.requireNonNull(profile.getPermVersion(), "permVersion must not be null");

		// 构建登录响应
		UserLoginResponse loginResponse = new UserLoginResponse();
		loginResponse.setId(userId);
		tokenService.buildToken(loginResponse, jti, permVersion);

		// 旋转刷新令牌
		String newRefreshToken = loginResponse.getRefreshToken();
		rotateRefreshInSession(jti, newRefreshToken);

		// 构建刷新令牌响应
		RefreshTokenResponse response = new RefreshTokenResponse();
		response.setAccessToken(loginResponse.getAccessToken());
		response.setRefreshToken(newRefreshToken);
		response.setExpires(loginResponse.getExpires());

		loginLogService.recordLoginLog(request, AuthLoginLogType.REFRESH_TOKEN, AuthLoginLogResult.SUCCESS, userId,
				null, null, jti);
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
			SecurityTokenResult tokenResult = tokenService.parseRefreshToken(refreshToken);
			return tokenResult.getUserToken().getUserId();
		}
		catch (Exception ignored) {
			return null;
		}
	}

	/**
	 * 解析并断言外部刷新令牌
	 * @param refreshToken 刷新令牌
	 * @return 安全令牌结果
	 */
	private SecurityTokenResult parseAndAssertExternalRefresh(String refreshToken) {
		// 校验刷新令牌非空
		if (CharSequenceUtil.isBlank(refreshToken)) {
			throw new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_MISSING);
		}
		SecurityTokenResult tokenResult;
		try {
			tokenResult = tokenService.parseRefreshToken(refreshToken);
		}
		catch (Exception ignored) {
			throw new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_MALFORMED);
		}
		if (tokenResult == null || tokenResult.getKind() != SecurityTokenKind.EXTERNAL_REFRESH) {
			throw new AuthBusinessException(AuthResultCode.AUTH_TYPE_UNSUPPORTED);
		}

		// 断言解析结果为外部刷新类型且携带完整会话声明
		JwtUserToken userToken = tokenResult.getUserToken();
		boolean hasUserToken = userToken != null && userToken.getUserId() != null
				&& CharSequenceUtil.isNotBlank(userToken.getJti());
		if (BooleanUtil.isFalse(hasUserToken)) {
			throw new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_MALFORMED);
		}
		return tokenResult;
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
		String redisHash = sessionIndex.getRefreshTokenHash();
		String requestHash = MD5.create().digestHex16(refreshToken);
		long now = System.currentTimeMillis();

		if (expiresAt == null || expiresAt <= now) {
			throw new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_EXPIRED);
		}

		// 刷新令牌与会话哈希不一致时撤销会话详情与活跃索引（幂等）
		boolean hashMatches = CharSequenceUtil.isNotBlank(redisHash) && Objects.equals(redisHash, requestHash);
		if (BooleanUtil.isFalse(hashMatches)) {
			userSessionIndexSession.deleteSession(jti);
			userSessionIndexSession.removeActiveSession(userId, jti);
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

		userSessionIndexSession.updateRefreshInfo(jti, refreshTokenHash, refreshTokenExpiresAt, ttl);
	}

}
