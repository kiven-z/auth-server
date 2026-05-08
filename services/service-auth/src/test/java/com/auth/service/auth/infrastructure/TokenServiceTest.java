package com.auth.service.auth.infrastructure;

import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import com.auth.module.security.core.token.provider.AccessTokenProvider;
import com.auth.module.security.core.token.provider.RefreshTokenProvider;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.infrastructure.login.TokenService;
import com.auth.service.auth.model.response.UserLoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
	@DisplayName("buildToken：填充 accessToken/refreshToken/expires")
	void buildToken_shouldFillAccessRefreshAndExpires() {
		// Arrange
		when(jwtProperties.getAccessExpired()).thenReturn(60L);
		when(accessTokenProvider.buildToken(TestConstants.USER_ID, TestConstants.JTI, 7L)).thenReturn("acc");
		when(refreshTokenProvider.buildToken(TestConstants.USER_ID, TestConstants.JTI, null)).thenReturn("ref");

		TokenService service = new TokenService(accessTokenProvider, refreshTokenProvider, jwtProperties);
		UserLoginResponse response = new UserLoginResponse();
		response.setId(TestConstants.USER_ID);

		// Act
		service.buildToken(response, TestConstants.JTI, 7L);

		// Assert
		assertEquals("acc", response.getAccessToken());
		assertEquals("ref", response.getRefreshToken());
		assertNotNull(response.getExpires());
	}

	@Test
	@DisplayName("parseAccessTokenOptional：空白 token 返回 empty")
	void parseAccessTokenOptional_shouldReturnEmpty_whenTokenBlank() {
		TokenService service = new TokenService(accessTokenProvider, refreshTokenProvider, jwtProperties);

		assertTrue(service.parseAccessTokenOptional(null).isEmpty());
		assertTrue(service.parseAccessTokenOptional("").isEmpty());
		assertTrue(service.parseAccessTokenOptional("   ").isEmpty());
		verifyNoInteractions(accessTokenProvider);
	}

	@Test
	@DisplayName("parseAccessTokenOptional：provider 抛异常时返回 empty")
	void parseAccessTokenOptional_shouldReturnEmpty_whenProviderThrows() {
		when(accessTokenProvider.parseToken("bad")).thenThrow(new RuntimeException("invalid jwt"));
		TokenService service = new TokenService(accessTokenProvider, refreshTokenProvider, jwtProperties);

		assertTrue(service.parseAccessTokenOptional("bad").isEmpty());
		verify(accessTokenProvider).parseToken("bad");
	}

	@Test
	@DisplayName("parseAccessTokenOptional：provider 返回 null 时返回 empty")
	void parseAccessTokenOptional_shouldReturnEmpty_whenProviderReturnsNull() {
		when(accessTokenProvider.parseToken("x")).thenReturn(null);
		TokenService service = new TokenService(accessTokenProvider, refreshTokenProvider, jwtProperties);

		assertTrue(service.parseAccessTokenOptional("x").isEmpty());
	}

	@Test
	@DisplayName("parseAccessTokenOptional：解析成功时返回非空 Optional")
	void parseAccessTokenOptional_shouldReturnPresent_whenProviderReturnsResult() {
		SecurityTokenResult expected = mock(SecurityTokenResult.class);
		when(accessTokenProvider.parseToken("ok")).thenReturn(expected);
		TokenService service = new TokenService(accessTokenProvider, refreshTokenProvider, jwtProperties);

		Optional<SecurityTokenResult> optional = service.parseAccessTokenOptional("ok");

		assertTrue(optional.isPresent());
		assertSame(expected, optional.get());
	}

}
