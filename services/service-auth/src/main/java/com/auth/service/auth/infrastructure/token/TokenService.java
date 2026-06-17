package com.auth.service.auth.infrastructure.token;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.core.token.provider.AccessTokenProvider;
import com.auth.module.security.core.token.provider.RefreshTokenProvider;
import com.auth.service.auth.application.port.login.TokenPort;
import com.auth.service.auth.domain.authentication.model.TokenPair;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.AuthResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 令牌服务：负责访问令牌与刷新令牌的签发、过期时间计算及后续解析。
 *
 * <p>
 * 本类不依赖 {@link jakarta.servlet.http.HttpServletRequest} 等 HTTP 上下文，适合在登录、 Token
 * 刷新、退出登录等不同场景下复用。
 * </p>
 *
 * @author Bunny
 */
@Slf4j
@Component
public class TokenService implements TokenPort {

	private final AccessTokenProvider accessTokenProvider;

	private final RefreshTokenProvider refreshTokenProvider;

	private final JwtProperties properties;

	public TokenService(AccessTokenProvider accessTokenProvider, RefreshTokenProvider refreshTokenProvider,
			JwtProperties properties) {
		this.accessTokenProvider = accessTokenProvider;
		this.refreshTokenProvider = refreshTokenProvider;
		this.properties = properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TokenPair buildTokenPair(Long userId, String jti, Long permVersion) {
		String accessToken = accessTokenProvider.buildToken(userId, jti, permVersion);
		String refreshToken = refreshTokenProvider.buildToken(userId, jti, null);
		return new TokenPair(accessToken, refreshToken, getAccessExpiresAt());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<JwtUserToken> parseAccessTokenSafe(String token) {
		if (CharSequenceUtil.isBlank(token)) {
			return Optional.empty();
		}
		try {
			// 解析访问令牌
			JwtUserToken jwtUserToken = requireUserToken(accessTokenProvider.parseToken(token).getUserToken(),
					AuthResultCode.TOKEN_PARSING_FAILED);

			return Optional.of(jwtUserToken);
		}
		catch (Exception exception) {
			log.error(exception.getMessage(), exception);
			return Optional.empty();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JwtUserToken parseRefreshToken(String token) {
		try {
			JwtUserToken userToken = refreshTokenProvider.parseToken(token).getUserToken();
			return requireUserToken(userToken, AuthResultCode.REFRESH_TOKEN_MALFORMED);
		}
		catch (AuthBusinessException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_MALFORMED);
		}
	}

	/**
	 * 获取访问令牌过期时间
	 * @return 过期时间
	 */
	public LocalDateTime getAccessExpiresAt() {
		long accessExpired = properties.getAccessExpired();
		return LocalDateTime.now().plusSeconds(accessExpired);
	}

	private JwtUserToken requireUserToken(JwtUserToken userToken, AuthResultCode code) {
		if (userToken == null || userToken.getUserId() == null || CharSequenceUtil.isBlank(userToken.getJti())) {
			throw new AuthBusinessException(code);
		}
		return userToken;
	}

}
