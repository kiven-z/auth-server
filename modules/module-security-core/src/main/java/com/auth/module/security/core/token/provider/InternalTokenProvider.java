package com.auth.module.security.core.token.provider;

import com.auth.common.jwt.api.JwtTokenProvider;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.constants.SecurityTokenKind;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static com.auth.module.security.contract.constants.SecurityExternalTokenConstants.TOKEN_TYPE;
import static com.auth.module.security.contract.constants.SecurityInternalTokenConstants.*;

/**
 * 内部令牌：双模签发——「用户身份」与「服务身份」共用一种令牌种类，由 {@code principal_type} claim 区分
 *
 * @author Bunny
 */
public class InternalTokenProvider extends AbstractSecurityTokenProvider {

	private final JwtProperties jwtProperties;

	public InternalTokenProvider(JwtTokenProvider jwtTokenProvider, JwtProperties jwtProperties) {
		super(jwtTokenProvider);
		this.jwtProperties = jwtProperties;
	}

	/**
	 * 构建用户身份内部令牌：sub = userId，{@code principal_type=USER}
	 * @param userId 用户ID
	 * @param jti 令牌ID
	 * @return 内部令牌
	 */
	@Override
	public String buildToken(Long userId, String jti, Long permVersion) {
		Objects.requireNonNull(userId, "userId is Null");

		// 构建 JWT 构建器
		JwtBuilder builder = baseBuilder(userId, jti).claims(Map.of(PRINCIPAL_TYPE, PRINCIPAL_TYPE_USER));
		// 生成 JWT 令牌
		return super.jwtTokenProvider.generatorJwtToken(builder);
	}

	public String buildToken(Long userId, String jti) {
		return buildToken(userId, jti, null);
	}

	/**
	 * 构建服务身份内部令牌：sub 占位为 真实服务名写入 {@code service_id} claim，{@code principal_type=SERVICE}
	 * @param serviceId 服务名（一般为 spring.application.name）
	 * @param jti 令牌ID
	 * @return 内部令牌
	 */
	public String buildServiceToken(String serviceId, String jti) {
		// 构建 JWT 构建器
		JwtBuilder builder = baseBuilder(SERVICE_SUB_PLACEHOLDER, jti)
			.claims(Map.of(PRINCIPAL_TYPE, PRINCIPAL_TYPE_SERVICE, SERVICE_ID, serviceId));
		// 生成 JWT 令牌
		return super.jwtTokenProvider.generatorJwtToken(builder);
	}

	/**
	 * 解析后从原始 claims 补齐 {@link SecurityTokenResult} 的 {@code principalType} /
	 * {@code serviceId}
	 */
	@Override
	@SuppressWarnings("java:S1854")
	protected void enrichTokenResult(SecurityTokenResult.SecurityTokenResultBuilder builder, String token,
			JwtUserToken jwtUserToken, SecurityTokenKind kind) {
		// 内部令牌类型不匹配，返回
		if (kind != SecurityTokenKind.INTERNAL) {
			return;
		}

		// 获取令牌声明
		Claims claims = super.jwtTokenProvider.getClaims(token);
		// 获取主体类型
		Object principalType = claims.get(PRINCIPAL_TYPE);
		if (principalType != null) {
			builder.principalType(principalType.toString());
		}

		// 获取服务ID
		Object serviceId = claims.get(SERVICE_ID);
		if (serviceId != null) {
			builder.serviceId(serviceId.toString());
		}
	}

	/**
	 * 共用基础 builder：填充 issuer / subject / iat / token_type / exp
	 */
	private JwtBuilder baseBuilder(Long userId, String jti) {
		String issuer = jwtProperties.getIssuer();
		String tokenKind = getTokenKind();
		return Jwts.builder()
			.id(jti)
			.issuer(issuer)
			.subject(String.valueOf(userId))
			.issuedAt(new Date())
			.claim(TOKEN_TYPE, tokenKind)
			.expiration(plusSeconds(new Date(), INTERNAL_MAX_TTL_SECONDS));
	}

	/**
	 * 获取令牌类型
	 * @return 令牌类型
	 */
	@Override
	protected String getTokenKind() {
		return SecurityTokenKind.INTERNAL.name();
	}

}
