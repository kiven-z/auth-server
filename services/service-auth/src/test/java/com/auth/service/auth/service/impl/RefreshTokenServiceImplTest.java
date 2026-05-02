package com.auth.service.auth.service.impl;

import cn.hutool.crypto.digest.MD5;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.api.SecurityTokenKind;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.infrastructure.login.TokenService;
import com.auth.service.auth.infrastructure.profile.AuthProfileCache;
import com.auth.service.auth.infrastructure.session.UserSessionIndexSession;
import com.auth.service.auth.model.enums.AuthLoginLogResult;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.response.RefreshTokenResponse;
import com.auth.service.auth.model.response.UserLoginResponse;
import com.auth.service.auth.service.AuthLoginAuditService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RefreshTokenServiceImpl 单元测试
 *
 * @author Bunny
 */
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

	@Test
	@DisplayName("刷新令牌：refreshToken 为空时抛 REFRESH_TOKEN_MISSING")
	void refreshToken_shouldThrowMissing_whenBlank() {
		RefreshTokenServiceImpl service = new RefreshTokenServiceImpl(authProfileCache, userSessionIndexSession,
				tokenService, jwtProperties, loginLogService);

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> service.refreshToken(" ", null));

		assertEquals(AuthResultCode.REFRESH_TOKEN_MISSING, exception.getResultCode());
		verify(loginLogService).recordLoginLog(isNull(), eq(AuthLoginLogType.REFRESH_TOKEN),
				eq(AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR), isNull(), isNull(), eq(exception.getMessageKey()),
				isNull());
	}

	@Test
	@DisplayName("刷新令牌：解析抛异常时抛 REFRESH_TOKEN_MALFORMED")
	void refreshToken_shouldThrowMalformed_whenParseThrows() {
		when(tokenService.parseRefreshToken(TestConstants.REFRESH_TOKEN)).thenThrow(new RuntimeException("boom"));
		RefreshTokenServiceImpl service = new RefreshTokenServiceImpl(authProfileCache, userSessionIndexSession,
				tokenService, jwtProperties, loginLogService);

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> service.refreshToken(TestConstants.REFRESH_TOKEN, null));

		assertEquals(AuthResultCode.REFRESH_TOKEN_MALFORMED, exception.getResultCode());
		verify(loginLogService).recordLoginLog(isNull(), eq(AuthLoginLogType.REFRESH_TOKEN),
				eq(AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR), isNull(), isNull(), eq(exception.getMessageKey()),
				isNull());
	}

	@Test
	@DisplayName("刷新令牌：kind 不是 EXTERNAL_REFRESH 时抛 AUTH_TYPE_UNSUPPORTED")
	void refreshToken_shouldThrowAuthTypeUnsupported_whenKindNotExternalRefresh() {
		SecurityTokenResult result = SecurityTokenResult.builder()
			.kind(SecurityTokenKind.EXTERNAL_ACCESS)
			.rawToken(TestConstants.REFRESH_TOKEN)
			.userToken(JwtUserToken.builder().userId(TestConstants.USER_ID).jti(TestConstants.JTI).build())
			.build();
		when(tokenService.parseRefreshToken(TestConstants.REFRESH_TOKEN)).thenReturn(result);

		RefreshTokenServiceImpl service = new RefreshTokenServiceImpl(authProfileCache, userSessionIndexSession,
				tokenService, jwtProperties, loginLogService);

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> service.refreshToken(TestConstants.REFRESH_TOKEN, null));

		assertEquals(AuthResultCode.AUTH_TYPE_UNSUPPORTED, exception.getResultCode());
		verify(loginLogService).recordLoginLog(isNull(), eq(AuthLoginLogType.REFRESH_TOKEN),
				eq(AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR), eq(TestConstants.USER_ID), isNull(),
				eq(exception.getMessageKey()), isNull());
	}

	@Test
	@DisplayName("刷新令牌：会话索引不存在时抛 REFRESH_TOKEN_EXPIRED")
	void refreshToken_shouldThrowExpired_whenSessionIndexMissing() {
		SecurityTokenResult result = SecurityTokenResult.builder()
			.kind(SecurityTokenKind.EXTERNAL_REFRESH)
			.rawToken(TestConstants.REFRESH_TOKEN)
			.userToken(JwtUserToken.builder().userId(TestConstants.USER_ID).jti(TestConstants.JTI).build())
			.build();
		when(tokenService.parseRefreshToken(TestConstants.REFRESH_TOKEN)).thenReturn(result);
		when(userSessionIndexSession.loadUserSessionIndex(TestConstants.JTI)).thenReturn(Optional.empty());

		RefreshTokenServiceImpl service = new RefreshTokenServiceImpl(authProfileCache, userSessionIndexSession,
				tokenService, jwtProperties, loginLogService);

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> service.refreshToken(TestConstants.REFRESH_TOKEN, null));

		assertEquals(AuthResultCode.REFRESH_TOKEN_EXPIRED, exception.getResultCode());
		verify(loginLogService).recordLoginLog(isNull(), eq(AuthLoginLogType.REFRESH_TOKEN),
				eq(AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR), eq(TestConstants.USER_ID), isNull(),
				eq(exception.getMessageKey()), isNull());
	}

	@Test
	@DisplayName("刷新令牌：会话 refresh 过期时抛 REFRESH_TOKEN_EXPIRED 且不撤销会话")
	void refreshToken_shouldThrowExpired_withoutRevoke_whenRefreshExpired() {
		SecurityTokenResult result = SecurityTokenResult.builder()
			.kind(SecurityTokenKind.EXTERNAL_REFRESH)
			.rawToken(TestConstants.REFRESH_TOKEN)
			.userToken(JwtUserToken.builder().userId(TestConstants.USER_ID).jti(TestConstants.JTI).build())
			.build();
		when(tokenService.parseRefreshToken(TestConstants.REFRESH_TOKEN)).thenReturn(result);

		UserSessionIndex expired = new UserSessionIndex();
		expired.setRefreshTokenExpiresAt(System.currentTimeMillis() - 1);
		expired.setRefreshTokenHash(MD5.create().digestHex16(TestConstants.REFRESH_TOKEN));
		when(userSessionIndexSession.loadUserSessionIndex(TestConstants.JTI)).thenReturn(Optional.of(expired));
		RefreshTokenServiceImpl service = new RefreshTokenServiceImpl(authProfileCache, userSessionIndexSession,
				tokenService, jwtProperties, loginLogService);
		assertEquals(AuthResultCode.REFRESH_TOKEN_EXPIRED,
				assertThrows(AuthBusinessException.class, () -> service.refreshToken(TestConstants.REFRESH_TOKEN, null))
					.getResultCode());
		verify(userSessionIndexSession, never()).deleteSession(anyString());
		verify(userSessionIndexSession, never()).removeActiveSession(anyLong(), anyString());
	}

	@Test
	@DisplayName("刷新令牌：哈希为空或不匹配时撤销会话后抛 REFRESH_TOKEN_EXPIRED")
	void refreshToken_shouldRevokeSession_thenThrowExpired_whenHashBlankOrMismatch() {
		SecurityTokenResult result = SecurityTokenResult.builder()
			.kind(SecurityTokenKind.EXTERNAL_REFRESH)
			.rawToken(TestConstants.REFRESH_TOKEN)
			.userToken(JwtUserToken.builder().userId(TestConstants.USER_ID).jti(TestConstants.JTI).build())
			.build();
		when(tokenService.parseRefreshToken(TestConstants.REFRESH_TOKEN)).thenReturn(result);

		UserSessionIndex hashBlank = new UserSessionIndex();
		hashBlank.setRefreshTokenExpiresAt(System.currentTimeMillis() + 1000);
		hashBlank.setRefreshTokenHash(" ");
		when(userSessionIndexSession.loadUserSessionIndex(TestConstants.JTI)).thenReturn(Optional.of(hashBlank));
		RefreshTokenServiceImpl service = new RefreshTokenServiceImpl(authProfileCache, userSessionIndexSession,
				tokenService, jwtProperties, loginLogService);
		assertEquals(AuthResultCode.REFRESH_TOKEN_EXPIRED,
				assertThrows(AuthBusinessException.class, () -> service.refreshToken(TestConstants.REFRESH_TOKEN, null))
					.getResultCode());
		verify(userSessionIndexSession).deleteSession(TestConstants.JTI);
		verify(userSessionIndexSession).removeActiveSession(TestConstants.USER_ID, TestConstants.JTI);

		reset(userSessionIndexSession);

		UserSessionIndex mismatch = new UserSessionIndex();
		mismatch.setRefreshTokenExpiresAt(System.currentTimeMillis() + 1000);
		mismatch.setRefreshTokenHash("deadbeef");
		when(userSessionIndexSession.loadUserSessionIndex(TestConstants.JTI)).thenReturn(Optional.of(mismatch));
		assertEquals(AuthResultCode.REFRESH_TOKEN_EXPIRED,
				assertThrows(AuthBusinessException.class, () -> service.refreshToken(TestConstants.REFRESH_TOKEN, null))
					.getResultCode());
		verify(userSessionIndexSession).deleteSession(TestConstants.JTI);
		verify(userSessionIndexSession).removeActiveSession(TestConstants.USER_ID, TestConstants.JTI);
	}

	@Test
	@DisplayName("刷新令牌：画像不存在时抛 REFRESH_TOKEN_EXPIRED")
	void refreshToken_shouldThrowExpired_whenAuthProfileMissing() {
		SecurityTokenResult result = SecurityTokenResult.builder()
			.kind(SecurityTokenKind.EXTERNAL_REFRESH)
			.rawToken(TestConstants.REFRESH_TOKEN)
			.userToken(JwtUserToken.builder().userId(TestConstants.USER_ID).jti(TestConstants.JTI).build())
			.build();
		when(tokenService.parseRefreshToken(TestConstants.REFRESH_TOKEN)).thenReturn(result);

		UserSessionIndex sessionIndex = new UserSessionIndex();
		sessionIndex.setRefreshTokenExpiresAt(System.currentTimeMillis() + 1000);
		sessionIndex.setRefreshTokenHash(MD5.create().digestHex16(TestConstants.REFRESH_TOKEN));
		when(userSessionIndexSession.loadUserSessionIndex(TestConstants.JTI)).thenReturn(Optional.of(sessionIndex));

		when(authProfileCache.loadCacheAuthProfile(TestConstants.USER_ID)).thenReturn(Optional.empty());

		RefreshTokenServiceImpl service = new RefreshTokenServiceImpl(authProfileCache, userSessionIndexSession,
				tokenService, jwtProperties, loginLogService);

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> service.refreshToken(TestConstants.REFRESH_TOKEN, null));

		assertEquals(AuthResultCode.REFRESH_TOKEN_EXPIRED, exception.getResultCode());
		verify(loginLogService).recordLoginLog(isNull(), eq(AuthLoginLogType.REFRESH_TOKEN),
				eq(AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR), eq(TestConstants.USER_ID), isNull(),
				eq(exception.getMessageKey()), isNull());
	}

	@Test
	@DisplayName("刷新令牌：成功时旋转 refresh token，并更新会话 TTL")
	void refreshToken_shouldRotateRefreshToken_andUpdateSessionTtl_whenSuccess() {
		long refreshExpiredSeconds = 120L;
		when(jwtProperties.getRefreshExpired()).thenReturn(refreshExpiredSeconds);

		SecurityTokenResult result = SecurityTokenResult.builder()
			.kind(SecurityTokenKind.EXTERNAL_REFRESH)
			.rawToken(TestConstants.REFRESH_TOKEN)
			.userToken(JwtUserToken.builder().userId(TestConstants.USER_ID).jti(TestConstants.JTI).build())
			.build();
		when(tokenService.parseRefreshToken(TestConstants.REFRESH_TOKEN)).thenReturn(result);

		UserSessionIndex sessionIndex = new UserSessionIndex();
		sessionIndex.setRefreshTokenExpiresAt(System.currentTimeMillis() + 60_000);
		sessionIndex.setRefreshTokenHash(MD5.create().digestHex16(TestConstants.REFRESH_TOKEN));
		when(userSessionIndexSession.loadUserSessionIndex(TestConstants.JTI)).thenReturn(Optional.of(sessionIndex));

		AuthProfile profile = mock(AuthProfile.class);
		when(profile.getPermVersion()).thenReturn(9L);
		when(authProfileCache.loadCacheAuthProfile(TestConstants.USER_ID)).thenReturn(Optional.of(profile));

		String newRefreshToken = "new-refresh";
		doAnswer(invocation -> {
			UserLoginResponse loginResponse = invocation.getArgument(0);
			loginResponse.setAccessToken("new-access");
			loginResponse.setRefreshToken(newRefreshToken);
			loginResponse.setExpires(LocalDateTime.now().plusSeconds(30));
			return null;
		}).when(tokenService).buildToken(any(UserLoginResponse.class), eq(TestConstants.JTI), eq(9L));

		RefreshTokenServiceImpl service = new RefreshTokenServiceImpl(authProfileCache, userSessionIndexSession,
				tokenService, jwtProperties, loginLogService);

		RefreshTokenResponse response = service.refreshToken(TestConstants.REFRESH_TOKEN, null);

		assertEquals("new-access", response.getAccessToken());
		assertEquals(newRefreshToken, response.getRefreshToken());
		assertNotNull(response.getExpires());

		String expectedHash = MD5.create().digestHex16(newRefreshToken);
		verify(userSessionIndexSession).updateRefreshInfo(eq(TestConstants.JTI), eq(expectedHash), anyLong(),
				eq(Duration.ofSeconds(refreshExpiredSeconds)));
		verify(loginLogService).recordLoginLog(isNull(), eq(AuthLoginLogType.REFRESH_TOKEN),
				eq(AuthLoginLogResult.SUCCESS), eq(TestConstants.USER_ID), isNull(), isNull(), eq(TestConstants.JTI));
	}

}
