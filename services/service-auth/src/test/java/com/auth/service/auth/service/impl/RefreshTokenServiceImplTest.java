package com.auth.service.auth.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.infrastructure.login.TokenPair;
import com.auth.service.auth.infrastructure.login.TokenService;
import com.auth.service.auth.infrastructure.profile.AuthProfileCache;
import com.auth.service.auth.infrastructure.session.UserSessionIndexSession;
import com.auth.service.auth.model.response.RefreshTokenResponse;
import com.auth.service.auth.service.AuthLoginAuditService;

import cn.hutool.crypto.digest.MD5;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

	@Mock
	private AuthProfileCache authProfileCache;

	@Mock
	private UserSessionIndexSession userSessionIndexSession;

	@Mock
	private TokenService tokenService;

	@Mock
	private JwtProperties jwtProperties;

	@Mock
	private AuthLoginAuditService loginLogService;

	private static JwtUserToken token(long userId) {
		return JwtUserToken.builder().userId(userId).jti(TestConstants.JTI).build();
	}

	private RefreshTokenServiceImpl newService() {
		return new RefreshTokenServiceImpl(authProfileCache, userSessionIndexSession, tokenService, jwtProperties,
				loginLogService);
	}

	@Test
	@DisplayName("刷新：会话不存在时抛 REFRESH_TOKEN_EXPIRED")
	void refresh_shouldThrowExpired_whenSessionMissing() {
		when(tokenService.parseRefreshToken(TestConstants.REFRESH_TOKEN)).thenReturn(token(TestConstants.USER_ID));
		when(userSessionIndexSession.loadUserSessionIndex(TestConstants.JTI)).thenReturn(Optional.empty());
		RefreshTokenServiceImpl service = newService();

		AuthBusinessException ex = assertThrows(AuthBusinessException.class,
				() -> service.refreshToken(TestConstants.REFRESH_TOKEN, null));

		assertEquals(AuthResultCode.REFRESH_TOKEN_EXPIRED, ex.getResultCode());
	}

	@Test
	@DisplayName("刷新：refresh 哈希不匹配时撤销会话并抛 REFRESH_TOKEN_EXPIRED")
	void refresh_shouldRevokeSession_whenRefreshHashMismatch() {
		when(tokenService.parseRefreshToken(TestConstants.REFRESH_TOKEN)).thenReturn(token(TestConstants.USER_ID));

		UserSessionIndex session = new UserSessionIndex();
		session.setUserId(TestConstants.USER_ID);
		session.setRefreshTokenExpiresAt(System.currentTimeMillis() + 60_000);
		session.setRefreshTokenHash("deadbeef");
		when(userSessionIndexSession.loadUserSessionIndex(TestConstants.JTI)).thenReturn(Optional.of(session));

		RefreshTokenServiceImpl service = newService();

		AuthBusinessException ex = assertThrows(AuthBusinessException.class,
				() -> service.refreshToken(TestConstants.REFRESH_TOKEN, null));

		assertEquals(AuthResultCode.REFRESH_TOKEN_EXPIRED, ex.getResultCode());
		verify(userSessionIndexSession).deleteSession(TestConstants.JTI);
		verify(userSessionIndexSession).removeActiveSession(TestConstants.USER_ID, TestConstants.JTI);
	}

	@Test
	@DisplayName("刷新：会话归属用户不一致时撤销会话并抛 REFRESH_TOKEN_EXPIRED")
	void refresh_shouldRevokeSession_whenSessionOwnershipMismatch() {
		when(tokenService.parseRefreshToken(TestConstants.REFRESH_TOKEN)).thenReturn(token(TestConstants.USER_ID));

		UserSessionIndex session = new UserSessionIndex();
		session.setUserId(TestConstants.USER_ID + 1L);
		session.setRefreshTokenExpiresAt(System.currentTimeMillis() + 60_000);
		session.setRefreshTokenHash(MD5.create().digestHex16(TestConstants.REFRESH_TOKEN));
		when(userSessionIndexSession.loadUserSessionIndex(TestConstants.JTI)).thenReturn(Optional.of(session));

		RefreshTokenServiceImpl service = newService();

		AuthBusinessException ex = assertThrows(AuthBusinessException.class,
				() -> service.refreshToken(TestConstants.REFRESH_TOKEN, null));

		assertEquals(AuthResultCode.REFRESH_TOKEN_EXPIRED, ex.getResultCode());
		verify(userSessionIndexSession).deleteSession(TestConstants.JTI);
		verify(userSessionIndexSession).removeActiveSession(TestConstants.USER_ID, TestConstants.JTI);
	}

	@Test
	@DisplayName("刷新：校验通过时返回新令牌并旋转 refresh")
	void refresh_shouldReturnNewTokenPair_whenAllChecksPass() {
		when(jwtProperties.getRefreshExpired()).thenReturn(120L);
		when(tokenService.parseRefreshToken(TestConstants.REFRESH_TOKEN)).thenReturn(token(TestConstants.USER_ID));

		UserSessionIndex session = new UserSessionIndex();
		session.setUserId(TestConstants.USER_ID);
		session.setRefreshTokenExpiresAt(System.currentTimeMillis() + 60_000);
		session.setRefreshTokenHash(MD5.create().digestHex16(TestConstants.REFRESH_TOKEN));
		when(userSessionIndexSession.loadUserSessionIndex(TestConstants.JTI)).thenReturn(Optional.of(session));

		AuthProfile profile = mock(AuthProfile.class);
		when(profile.getPermVersion()).thenReturn(9L);
		when(authProfileCache.loadCacheAuthProfile(TestConstants.USER_ID)).thenReturn(Optional.of(profile));

		LocalDateTime expires = LocalDateTime.now().plusMinutes(5);
		when(tokenService.buildTokenPair(TestConstants.USER_ID, TestConstants.JTI, 9L))
			.thenReturn(new TokenPair("new-access", "new-refresh", expires));

		RefreshTokenServiceImpl service = newService();

		RefreshTokenResponse response = service.refreshToken(TestConstants.REFRESH_TOKEN, null);

		assertEquals("new-access", response.getAccessToken());
		assertEquals("new-refresh", response.getRefreshToken());
		assertEquals(expires, response.getExpires());
		verify(userSessionIndexSession).updateRefreshInfo(eq(TestConstants.JTI),
				eq(MD5.create().digestHex16("new-refresh")), anyLong(), eq(Duration.ofSeconds(120L)));
	}

}
