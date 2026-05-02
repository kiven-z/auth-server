package com.auth.module.security.core.token.facade;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import com.auth.module.security.core.token.provider.AccessTokenProvider;
import com.auth.module.security.core.token.provider.InternalTokenProvider;
import com.auth.module.security.core.token.provider.RefreshTokenProvider;

import static com.auth.common.jwt.provider.AbstractJwtTokenProvider.BEARER_PREFIX;

/**
 * 安全令牌验证器（无 Servlet 依赖）
 *
 * @author Bunny
 */
public class SecurityTokenHandlerFacade {

	private final AccessTokenProvider accessTokenProvider;

	private final RefreshTokenProvider refreshTokenProvider;

	private final InternalTokenProvider internalTokenProvider;

	public SecurityTokenHandlerFacade(AccessTokenProvider accessTokenProvider,
			RefreshTokenProvider refreshTokenProvider, InternalTokenProvider internalTokenProvider) {
		this.accessTokenProvider = accessTokenProvider;
		this.refreshTokenProvider = refreshTokenProvider;
		this.internalTokenProvider = internalTokenProvider;
	}

	/**
	 * 解析 Authorization 头部，提取 Bearer token 允许输入本身就是 token（兼容部分旧代码）
	 */
	private static String extractBearerToken(String authorizationHeader) {
		if (CharSequenceUtil.isBlank(authorizationHeader)) {
			return null;
		}
		String value = authorizationHeader.trim();
		if (CharSequenceUtil.startWithIgnoreCase(value, BEARER_PREFIX)) {
			return value.substring(BEARER_PREFIX.length()).trim();
		}
		return value;
	}

	/**
	 * 从头部验证令牌（internal 优先，否则 external）
	 * @param authorizationHeader 授权头部
	 * @param internalJwtHeader 内部 JWT 头部
	 * @return 安全令牌结果
	 */
	public SecurityTokenResult validateFromHeaders(String authorizationHeader, String internalJwtHeader) {
		// 先验证内部令牌
		if (CharSequenceUtil.isNotBlank(internalJwtHeader)) {
			return validateInternal(internalJwtHeader.trim());
		}

		// 再验证外部令牌
		if (CharSequenceUtil.isNotBlank(authorizationHeader)) {
			return validateExternal(authorizationHeader);
		}

		throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_MISSING, "Token is missing.");
	}

	/**
	 * 兼容旧入口：从内部令牌验证令牌
	 * @param internalJwtHeader 令牌
	 * @return 安全令牌结果
	 */
	public SecurityTokenResult validateInternal(String internalJwtHeader) {
		if (CharSequenceUtil.isBlank(internalJwtHeader)) {
			throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_MISSING, "Internal token is missing.");
		}

		boolean validated = internalTokenProvider.verifyToken(internalJwtHeader);
		if (!validated) {
			throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_INVALID, "Internal token is invalid.");
		}
		return internalTokenProvider.parseToken(internalJwtHeader);
	}

	/**
	 * 验证外部令牌
	 * @param authorizationHeader 授权头部
	 * @return 安全令牌结果
	 */
	public SecurityTokenResult validateExternal(String authorizationHeader) {
		String token = extractBearerToken(authorizationHeader);
		if (CharSequenceUtil.isBlank(token)) {
			throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_MISSING, "External token is missing.");
		}

		// 优先按 access token 校验；如果未来你要支持 refresh token 走这里，可以放开 fallback
		if (accessTokenProvider.verifyToken(token)) {
			return accessTokenProvider.parseToken(token);
		}
		if (refreshTokenProvider.verifyToken(token)) {
			return refreshTokenProvider.parseToken(token);
		}
		throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_INVALID, "External token is invalid.");
	}

}
