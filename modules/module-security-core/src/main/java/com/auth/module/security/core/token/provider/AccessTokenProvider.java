package com.auth.module.security.core.token.provider;

import static com.auth.module.security.contract.constants.SecurityExternalTokenConstants.*;
import static com.auth.module.security.core.token.support.SecurityTokenSupport.*;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import com.auth.common.jwt.api.JwtTokenProvider;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.constants.SecurityTokenKind;
import com.auth.module.security.core.token.model.SecurityTokenResult;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

/**
 * 访问令牌支持
 *
 * @author Bunny
 */
public class AccessTokenProvider extends AbstractSecurityTokenProvider {

	private final JwtProperties properties;

	public AccessTokenProvider(JwtTokenProvider jwtTokenProvider, JwtProperties properties) {
		super(jwtTokenProvider);
		this.properties = properties;
	}

	/**
	 * 构建访问令牌（不携带权限版本快照）
	 * @param userId 用户 ID
	 * @param jti 令牌 ID（会话绑定）
	 * @return 访问令牌
	 */
	@Override
	public String buildToken(Long userId, String jti, Long permVersion) {
		Objects.requireNonNull(userId, "userId is Null");
		Objects.requireNonNull(permVersion, "permVersion is Null");

		String issuer = properties.getIssuer();
		long accessExpired = properties.getAccessExpired();

		JwtBuilder jwtBuilder = Jwts.builder()
			.id(jti)
			.issuer(issuer)
			.subject(String.valueOf(userId))
			.issuedAt(new Date())
			.claims(Map.of(
					// Token类型
					TOKEN_TYPE, getTokenKind(),
					// 版本号
					PERM_VERSION, permVersion))
			.expiration(plusSeconds(new Date(), accessExpired));
		return super.jwtTokenProvider.generatorJwtToken(jwtBuilder);
	}

	/**
	 * 填充令牌结果
	 * @param builder 令牌结果构建器
	 * @param token 令牌
	 * @param jwtUserToken 令牌用户
	 * @param kind 令牌类型
	 */
	@Override
	@SuppressWarnings("java:S1854")
	protected void enrichTokenResult(SecurityTokenResult.SecurityTokenResultBuilder builder, String token,
			JwtUserToken jwtUserToken, SecurityTokenKind kind, boolean allowExpired) {
		if (kind != SecurityTokenKind.EXTERNAL_ACCESS) {
			return;
		}

		Claims claims = allowExpired ? jwtTokenProvider.getClaimsAllowExpired(token)
				: jwtTokenProvider.getClaims(token);
		Object raw = claims.get(PERM_VERSION);
		Long permVersion = parsePermVersionClaim(raw);
		builder.permVersion(permVersion);
	}

	/**
	 * 获取令牌类型
	 * @return 令牌类型
	 */
	@Override
	protected String getTokenKind() {
		return SecurityTokenKind.EXTERNAL_ACCESS.name();
	}

}
