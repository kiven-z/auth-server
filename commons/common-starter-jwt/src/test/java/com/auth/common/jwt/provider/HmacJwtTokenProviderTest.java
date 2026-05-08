package com.auth.common.jwt.provider;

import static com.auth.common.jwt.model.SignatureAlgorithm.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.exception.JwtExpiredException;
import com.auth.common.jwt.key.HmacSecretKeys;

import cn.hutool.core.util.IdUtil;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

class HmacJwtTokenProviderTest {

	private static final String issuer = "test-issuer";

	private static final long userId = 1L;

	@Test
	@DisplayName("生成访问令牌、刷新令牌并验证")
	void mintAccessRefreshValidateAndRemaining() {
		JwtProperties props = new JwtProperties();
		props.setAlgorithm(HS256);
		props.setIssuer(issuer);
		props.setSecret("0123456789abcdef0123456789abcdef0123456789abcdef0123456789ab");
		props.setAccessExpired(3600);
		props.setRefreshExpired(7200);
		props.validate();
		HmacJwtTokenProvider provider = new HmacJwtTokenProvider(props,
				HmacSecretKeys.fromUtf8Secret(props.getSecret()));

		// 访问令牌
		JwtBuilder accessTokenJwtBuilder = Jwts.builder()
			.id(IdUtil.fastSimpleUUID())
			.issuer(issuer)
			.subject(String.valueOf(userId))
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + 3600 * 1000));
		String access = provider.generatorJwtToken(accessTokenJwtBuilder);
		assertTrue(provider.validateToken(access));
		assertEquals(userId, provider.parseToken(access).getUserId());
		long remaining = provider.getRemainingSeconds(access);
		assertTrue(remaining > 0);

		// 刷新令牌
		JwtBuilder refreshTokenJwtBuilder = Jwts.builder()
			.id(IdUtil.fastSimpleUUID())
			.issuer(issuer)
			.subject(String.valueOf(userId))
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + 3600 * 7000));
		String refresh = provider.generatorJwtToken(refreshTokenJwtBuilder);
		assertTrue(provider.validateToken(refresh));
		assertFalse(provider.validateToken(""));
		assertFalse(provider.validateToken(null));
	}

	@Test
	@DisplayName("允许过期解析：过期令牌可读取 claims 与 userToken")
	void parseAllowExpired_shouldReadClaimsAndUserToken_whenTokenExpired() {
		JwtProperties props = new JwtProperties();
		props.setAlgorithm(HS256);
		props.setIssuer(issuer);
		props.setSecret("0123456789abcdef0123456789abcdef0123456789abcdef0123456789ab");
		props.setAccessExpired(3600);
		props.setRefreshExpired(7200);
		props.validate();
		HmacJwtTokenProvider provider = new HmacJwtTokenProvider(props,
				HmacSecretKeys.fromUtf8Secret(props.getSecret()));

		String expiredToken = provider.generatorJwtToken(Jwts.builder()
			.id(IdUtil.fastSimpleUUID())
			.issuer(issuer)
			.subject(String.valueOf(userId))
			.issuedAt(new Date(System.currentTimeMillis() - 240_000))
			.expiration(new Date(System.currentTimeMillis() - 120_000)));

		assertThrows(JwtExpiredException.class, () -> provider.parseToken(expiredToken));
		assertEquals(String.valueOf(userId), provider.getClaimsAllowExpired(expiredToken).getSubject());
		assertEquals(userId, provider.parseTokenAllowExpired(expiredToken).getUserId());
	}

}
