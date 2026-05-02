package com.auth.service.auth.infrastructure.login;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import com.auth.module.security.core.token.provider.AccessTokenProvider;
import com.auth.module.security.core.token.provider.RefreshTokenProvider;
import com.auth.service.auth.model.response.UserLoginResponse;
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
@Component
public class TokenService {

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
	 * 为登录响应填充访问令牌、刷新令牌及过期时间
	 * @param response 用户登录响应（将被修改）
	 * @param jti 会话唯一标识
	 * @param permVersion 权限版本快照
	 */
	public void buildToken(UserLoginResponse response, String jti, long permVersion) {
		Long userId = response.getId();

		// 设置访问令牌
		String accessToken = accessTokenProvider.buildToken(userId, jti, permVersion);
		response.setAccessToken(accessToken);

		// 设置刷新令牌
		String refreshToken = refreshTokenProvider.buildToken(userId, jti, null);
		response.setRefreshToken(refreshToken);

		// 设置访问令牌过期时间
		long accessExpired = properties.getAccessExpired();
		response.setExpires(LocalDateTime.now().plusSeconds(accessExpired));
	}

	/**
	 * 解析访问令牌
	 * @param token 访问令牌原始字符串
	 * @return 解析结果
	 */
	public SecurityTokenResult parseAccessToken(String token) {
		return accessTokenProvider.parseToken(token);
	}

	/**
	 * 容错解析访问令牌（永不抛出异常）。
	 *
	 * <p>
	 * 适用于登出等「解析失败即视为无服务端会话可清理」的场景；需要严格失败语义时请使用 {@link #parseAccessToken(String)}。
	 * </p>
	 * @param token 访问令牌原始字符串
	 * @return 解析成功且非 {@code null} 时为 {@link Optional#of(Object)}，否则为空
	 */
	public Optional<SecurityTokenResult> parseAccessTokenOptional(String token) {
		if (CharSequenceUtil.isBlank(token)) {
			return Optional.empty();
		}
		try {
			return Optional.ofNullable(accessTokenProvider.parseToken(token));
		}
		catch (Exception ignored) {
			return Optional.empty();
		}
	}

	/**
	 * 解析刷新令牌
	 * @param token 刷新令牌原始字符串
	 * @return 解析结果
	 */
	public SecurityTokenResult parseRefreshToken(String token) {
		return refreshTokenProvider.parseToken(token);
	}

}
