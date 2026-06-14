package com.auth.service.auth.application.port;

import com.auth.common.jwt.model.JwtUserToken;
import com.auth.service.auth.domain.authentication.model.TokenPair;

import java.util.Optional;

/**
 * JWT 令牌网关：签发访问/刷新令牌及解析。
 *
 * @author Bunny
 */
public interface TokenGateway {

	/**
	 * 签发访问令牌与刷新令牌。
	 * @param userId 用户 ID
	 * @param jti 会话唯一标识
	 * @param permVersion 权限版本快照
	 * @return 令牌对
	 */
	TokenPair buildTokenPair(Long userId, String jti, Long permVersion);

	/**
	 * 容错解析访问令牌（永不抛出异常）。
	 * @param token 访问令牌原始字符串
	 * @return 用户载荷
	 */
	Optional<JwtUserToken> parseAccessTokenSafe(String token);

	/**
	 * 解析刷新令牌。
	 * @param token 刷新令牌原始字符串
	 * @return 用户载荷
	 */
	JwtUserToken parseRefreshToken(String token);

}
