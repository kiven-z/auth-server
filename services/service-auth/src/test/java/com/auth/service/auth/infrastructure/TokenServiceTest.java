package com.auth.service.auth.infrastructure;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import com.auth.module.security.core.token.provider.AccessTokenProvider;
import com.auth.module.security.core.token.provider.RefreshTokenProvider;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.infrastructure.login.TokenPair;
import com.auth.service.auth.infrastructure.login.TokenService;

/**
 * TokenService 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

	@Mock
	private AccessTokenProvider accessTokenProvider;

	@Mock
	private RefreshTokenProvider refreshTokenProvider;

	@Mock
	private JwtProperties jwtProperties;

	@Test
	@DisplayName("buildTokenPair：签发访问与刷新令牌")
	void buildTokenPair_shouldReturnTokenPair() {
		// Arrange
		when(jwtProperties.getAccessExpired()).thenReturn(60L);
		when(accessTokenProvider.buildToken(TestConstants.USER_ID, TestConstants.JTI, 7L)).thenReturn("acc");
		when(refreshTokenProvider.buildToken(TestConstants.USER_ID, TestConstants.JTI, null)).thenReturn("ref");

		TokenService service = new TokenService(accessTokenProvider, refreshTokenProvider, jwtProperties);

		// Act
		TokenPair tokenPair = service.buildTokenPair(TestConstants.USER_ID, TestConstants.JTI, 7L);

		// Assert
		assertEquals("acc", tokenPair.accessToken());
		assertEquals("ref", tokenPair.refreshToken());
		assertNotNull(tokenPair.accessExpiresAt());
	}

	@Test
	@DisplayName("parseAccessToken：解析成功时返回用户载荷")
	void parseAccessToken_shouldReturnUserToken_whenSuccess() {
		JwtUserToken userToken = JwtUserToken.builder().userId(TestConstants.USER_ID).jti(TestConstants.JTI).build();
		SecurityTokenResult result = SecurityTokenResult.builder().userToken(userToken).build();
		when(accessTokenProvider.parseToken("ok")).thenReturn(result);
		TokenService service = new TokenService(accessTokenProvider, refreshTokenProvider, jwtProperties);

		JwtUserToken parsed = service.parseAccessToken("ok");

		assertEquals(TestConstants.USER_ID, parsed.getUserId());
		assertEquals(TestConstants.JTI, parsed.getJti());
	}

	@Test
	@DisplayName("parseAccessToken：provider 异常时抛 TOKEN_PARSING_FAILED")
	void parseAccessToken_shouldThrowBusinessException_whenProviderThrows() {
		when(accessTokenProvider.parseToken("bad")).thenThrow(new RuntimeException("invalid jwt"));
		TokenService service = new TokenService(accessTokenProvider, refreshTokenProvider, jwtProperties);

		AuthBusinessException ex = assertThrows(AuthBusinessException.class, () -> service.parseAccessToken("bad"));
		assertEquals(AuthResultCode.TOKEN_PARSING_FAILED, ex.getResultCode());
	}

	@Test
	@DisplayName("parseAccessTokenSafe：空白或异常时返回 empty")
	void parseAccessTokenSafe_shouldReturnEmpty_whenBlankOrThrows() {
		TokenService service = new TokenService(accessTokenProvider, refreshTokenProvider, jwtProperties);
		when(accessTokenProvider.parseToken("bad")).thenThrow(new RuntimeException("invalid jwt"));

		assertTrue(service.parseAccessTokenSafe(null).isEmpty());
		assertTrue(service.parseAccessTokenSafe(" ").isEmpty());
		assertTrue(service.parseAccessTokenSafe("bad").isEmpty());
	}

	@Test
	@DisplayName("parseExpiredAccessToken：调用允许过期解析")
	void parseExpiredAccessToken_shouldUseAllowExpiredProvider() {
		JwtUserToken userToken = JwtUserToken.builder().userId(TestConstants.USER_ID).jti(TestConstants.JTI).build();
		SecurityTokenResult result = SecurityTokenResult.builder().userToken(userToken).build();
		when(accessTokenProvider.parseTokenAllowExpired("expired")).thenReturn(result);
		TokenService service = new TokenService(accessTokenProvider, refreshTokenProvider, jwtProperties);

		Optional<JwtUserToken> optional = service.parseExpiredAccessToken("expired");

		assertTrue(optional.isPresent());
		assertEquals(TestConstants.USER_ID, optional.get().getUserId());
		verify(accessTokenProvider).parseTokenAllowExpired("expired");
	}

	@Test
	@DisplayName("parseRefreshToken：解析失败时抛 REFRESH_TOKEN_MALFORMED")
	void parseRefreshToken_shouldThrowBusinessException_whenParseFailed() {
		when(refreshTokenProvider.parseToken("bad")).thenThrow(new RuntimeException("bad refresh"));
		TokenService service = new TokenService(accessTokenProvider, refreshTokenProvider, jwtProperties);

		AuthBusinessException ex = assertThrows(AuthBusinessException.class, () -> service.parseRefreshToken("bad"));
		assertEquals(AuthResultCode.REFRESH_TOKEN_MALFORMED, ex.getResultCode());
	}

	@Test
	@DisplayName("getAccessExpiresAt：根据配置计算过期时间")
	void getAccessExpiresAt_shouldReturnFutureTime() {
		when(jwtProperties.getAccessExpired()).thenReturn(30L);
		TokenService service = new TokenService(accessTokenProvider, refreshTokenProvider, jwtProperties);

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime expiresAt = service.getAccessExpiresAt();

		assertTrue(expiresAt.isAfter(now));
	}

}
