package com.auth.service.auth.service.impl;

import cn.hutool.crypto.digest.MD5;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.model.enums.AuthResultCode;
import com.auth.service.auth.infrastructure.login.TokenPair;
import com.auth.service.auth.infrastructure.login.TokenService;
import com.auth.service.auth.infrastructure.profile.AuthProfileCache;
import com.auth.service.auth.infrastructure.session.redis.RedisUserSessionIndexRepository;
import com.auth.service.auth.model.enums.AuthLoginLogResult;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.response.RefreshTokenResponse;
import com.auth.service.auth.service.AuthLoginAuditService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

	@Mock
	private AuthProfileCache authProfileCache;

	@Mock
	private RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	@Mock
	private TokenService tokenService;

	@Mock
	private JwtProperties jwtProperties;

	@Mock
	private AuthLoginAuditService loginLogService;

	private static JwtUserToken token() {
		return JwtUserToken.builder().userId(TestConstants.USER_ID).jti(TestConstants.JTI).build();
	}

	private RefreshTokenServiceImpl newService() {
		return new RefreshTokenServiceImpl(authProfileCache, redisUserSessionIndexRepository, tokenService,
				jwtProperties, loginLogService);
	}

	@Test
	@DisplayName("刷新：会话不存在时抛 REFRESH_TOKEN_EXPIRED")
	void refresh_shouldThrowExpired_whenSessionMissing() {
		when(tokenService.parseRefreshToken(TestConstants.REFRESH_TOKEN)).thenReturn(token());
		when(redisUserSessionIndexRepository.loadUserSessionIndex(TestConstants.JTI)).thenReturn(Optional.empty());
		RefreshTokenServiceImpl service = newService();

		AuthBusinessException ex = assertThrows(AuthBusinessException.class,
				() -> service.refreshToken(TestConstants.REFRESH_TOKEN, null));

		assertEquals(AuthResultCode.REFRESH_TOKEN_EXPIRED, ex.getResultCode());
	}

	@Test
	@DisplayName("刷新：refresh 哈希不匹配时撤销会话并抛 REFRESH_TOKEN_EXPIRED")
	void refresh_shouldRevokeSession_whenRefreshHashMismatch() {
		when(tokenService.parseRefreshToken(TestConstants.REFRESH_TOKEN)).thenReturn(token());

		UserSessionIndex session = new UserSessionIndex();
		session.setUserId(TestConstants.USER_ID);
		session.setRefreshTokenExpiresAt(System.currentTimeMillis() + 60_000);
		session.setRefreshTokenHash("deadbeef");
		when(redisUserSessionIndexRepository.loadUserSessionIndex(TestConstants.JTI)).thenReturn(Optional.of(session));

		RefreshTokenServiceImpl service = newService();

		AuthBusinessException ex = assertThrows(AuthBusinessException.class,
				() -> service.refreshToken(TestConstants.REFRESH_TOKEN, null));

		assertEquals(AuthResultCode.REFRESH_TOKEN_EXPIRED, ex.getResultCode());
		verify(redisUserSessionIndexRepository).deleteSession(TestConstants.JTI);
		verify(redisUserSessionIndexRepository).removeActiveSession(TestConstants.USER_ID, TestConstants.JTI);
	}

	@Test
	@DisplayName("刷新：会话归属用户不一致时撤销会话并抛 REFRESH_TOKEN_EXPIRED")
	void refresh_shouldRevokeSession_whenSessionOwnershipMismatch() {
		when(tokenService.parseRefreshToken(TestConstants.REFRESH_TOKEN)).thenReturn(token());

		UserSessionIndex session = new UserSessionIndex();
		session.setUserId(TestConstants.USER_ID + 1L);
		session.setRefreshTokenExpiresAt(System.currentTimeMillis() + 60_000);
		session.setRefreshTokenHash(MD5.create().digestHex16(TestConstants.REFRESH_TOKEN));
		when(redisUserSessionIndexRepository.loadUserSessionIndex(TestConstants.JTI)).thenReturn(Optional.of(session));

		RefreshTokenServiceImpl service = newService();

		AuthBusinessException ex = assertThrows(AuthBusinessException.class,
				() -> service.refreshToken(TestConstants.REFRESH_TOKEN, null));

		assertEquals(AuthResultCode.REFRESH_TOKEN_EXPIRED, ex.getResultCode());
		verify(redisUserSessionIndexRepository).deleteSession(TestConstants.JTI);
		verify(redisUserSessionIndexRepository).removeActiveSession(TestConstants.USER_ID, TestConstants.JTI);
	}

	@Test
	@DisplayName("刷新：空白刷新令牌时抛 REFRESH_TOKEN_MISSING 并记录审计")
	void refresh_shouldThrowMissing_whenTokenBlank() {
		RefreshTokenServiceImpl service = newService();

		AuthBusinessException ex = assertThrows(AuthBusinessException.class, () -> service.refreshToken(" ", null));

		assertEquals(AuthResultCode.REFRESH_TOKEN_MISSING, ex.getResultCode());
		verify(loginLogService).recordLoginLog(isNull(), eq(AuthLoginLogType.REFRESH_TOKEN),
				eq(AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR), isNull(), eq("/"), eq(ex.getMessageKey()),
				isNull());
	}

	@Test
	@DisplayName("刷新：tokenService 解析 refreshToken 异常时抛 REFRESH_TOKEN_MALFORMED 并记录审计")
	void refresh_shouldThrowMalformed_whenParseFails() {
		when(tokenService.parseRefreshToken(TestConstants.REFRESH_TOKEN))
			.thenThrow(new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_MALFORMED));
		RefreshTokenServiceImpl service = newService();

		AuthBusinessException ex = assertThrows(AuthBusinessException.class,
				() -> service.refreshToken(TestConstants.REFRESH_TOKEN, null));

		assertEquals(AuthResultCode.REFRESH_TOKEN_MALFORMED, ex.getResultCode());
		verify(loginLogService).recordLoginLog(isNull(), eq(AuthLoginLogType.REFRESH_TOKEN),
				eq(AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR), isNull(), eq("/"), eq(ex.getMessageKey()),
				isNull());
	}

	@Test
	@DisplayName("刷新：校验通过时返回新令牌并旋转 refresh")
	void refresh_shouldReturnNewTokenPair_whenAllChecksPass() {
		when(jwtProperties.getRefreshExpired()).thenReturn(120L);
		when(tokenService.parseRefreshToken(TestConstants.REFRESH_TOKEN)).thenReturn(token());

		UserSessionIndex session = new UserSessionIndex();
		session.setUserId(TestConstants.USER_ID);
		session.setRefreshTokenExpiresAt(System.currentTimeMillis() + 60_000);
		session.setRefreshTokenHash(MD5.create().digestHex16(TestConstants.REFRESH_TOKEN));
		when(redisUserSessionIndexRepository.loadUserSessionIndex(TestConstants.JTI)).thenReturn(Optional.of(session));

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
		verify(redisUserSessionIndexRepository).updateRefreshInfo(eq(TestConstants.JTI),
				eq(MD5.create().digestHex16("new-refresh")), anyLong(), eq(Duration.ofSeconds(120L)));
	}

}
