package com.auth.service.auth.authentication.application;

import cn.hutool.crypto.digest.MD5;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogResult;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.event.LoginAuditEventPublisher;
import com.auth.service.auth.authentication.domain.model.RefreshSessionResult;
import com.auth.service.auth.authentication.domain.model.TokenPair;
import com.auth.service.auth.authentication.infrastructure.token.TokenService;
import com.auth.service.auth.authorization.application.AuthProfileCacheApplicationService;
import com.auth.service.auth.session.infrastructure.redis.RedisUserSessionIndexRepository;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
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
 * {@link RefreshTokenService} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

	@Mock
	private AuthProfileCacheApplicationService authProfileCacheApplicationService;

	@Mock
	private RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	@Mock
	private TokenService tokenService;

	@Mock
	private JwtProperties jwtProperties;

	@Mock
	private LoginAuditEventPublisher loginAuditEventPublisher;

	private static JwtUserToken token() {
		return JwtUserToken.builder().userId(TestConstants.USER_ID).jti(TestConstants.JTI).build();
	}

	private RefreshTokenService newService() {
		return new RefreshTokenService(authProfileCacheApplicationService, redisUserSessionIndexRepository,
				tokenService, jwtProperties, loginAuditEventPublisher);
	}

	@Test
	@DisplayName("刷新：会话不存在时抛 REFRESH_TOKEN_EXPIRED")
	void refresh_shouldThrowExpired_whenSessionMissing() {
		when(tokenService.parseRefreshToken(TestConstants.REFRESH_TOKEN)).thenReturn(token());
		when(redisUserSessionIndexRepository.loadUserSessionIndex(TestConstants.JTI)).thenReturn(Optional.empty());
		RefreshTokenService service = newService();

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

		RefreshTokenService service = newService();

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

		RefreshTokenService service = newService();

		AuthBusinessException ex = assertThrows(AuthBusinessException.class,
				() -> service.refreshToken(TestConstants.REFRESH_TOKEN, null));

		assertEquals(AuthResultCode.REFRESH_TOKEN_EXPIRED, ex.getResultCode());
		verify(redisUserSessionIndexRepository).deleteSession(TestConstants.JTI);
		verify(redisUserSessionIndexRepository).removeActiveSession(TestConstants.USER_ID, TestConstants.JTI);
	}

	@Test
	@DisplayName("刷新：空白刷新令牌时抛 REFRESH_TOKEN_MISSING 并记录审计")
	void refresh_shouldThrowMissing_whenTokenBlank() {
		RefreshTokenService service = newService();

		AuthBusinessException ex = assertThrows(AuthBusinessException.class, () -> service.refreshToken(" ", null));

		assertEquals(AuthResultCode.REFRESH_TOKEN_MISSING, ex.getResultCode());
		verify(loginAuditEventPublisher).publish(isNull(), eq(AuthLoginLogType.REFRESH_TOKEN),
				eq(AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR), isNull(), eq("/"), eq(ex.getMessageKey()),
				isNull());
	}

	@Test
	@DisplayName("刷新：tokenService 解析 refreshToken 异常时抛 REFRESH_TOKEN_MALFORMED 并记录审计")
	void refresh_shouldThrowMalformed_whenParseFails() {
		when(tokenService.parseRefreshToken(TestConstants.REFRESH_TOKEN))
			.thenThrow(new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_MALFORMED));
		RefreshTokenService service = newService();

		AuthBusinessException ex = assertThrows(AuthBusinessException.class,
				() -> service.refreshToken(TestConstants.REFRESH_TOKEN, null));

		assertEquals(AuthResultCode.REFRESH_TOKEN_MALFORMED, ex.getResultCode());
		verify(loginAuditEventPublisher).publish(isNull(), eq(AuthLoginLogType.REFRESH_TOKEN),
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
		when(authProfileCacheApplicationService.loadCachedProfile(TestConstants.USER_ID))
			.thenReturn(Optional.of(profile));

		LocalDateTime expires = LocalDateTime.now().plusMinutes(5);
		TokenPair tokenPair = new TokenPair("new-access", "new-refresh", expires);
		when(tokenService.buildTokenPair(TestConstants.USER_ID, TestConstants.JTI, 9L)).thenReturn(tokenPair);

		RefreshTokenService service = newService();

		RefreshSessionResult result = service.refreshToken(TestConstants.REFRESH_TOKEN, null);

		assertEquals("new-access", result.tokenPair().accessToken());
		assertEquals("new-refresh", result.tokenPair().refreshToken());
		assertEquals(expires, result.tokenPair().accessExpiresAt());
		assertFalse(result.rememberMe());
		verify(redisUserSessionIndexRepository).updateRefreshInfo(eq(TestConstants.JTI),
				eq(MD5.create().digestHex16("new-refresh")), anyLong(), eq(Duration.ofSeconds(120L)));
	}

	@Test
	@DisplayName("刷新：会话 rememberMe=true 时结果携带 rememberMe")
	void refresh_shouldReturnRememberMeFromSession() {
		when(jwtProperties.getRefreshExpired()).thenReturn(120L);
		when(tokenService.parseRefreshToken(TestConstants.REFRESH_TOKEN)).thenReturn(token());

		UserSessionIndex session = new UserSessionIndex();
		session.setUserId(TestConstants.USER_ID);
		session.setRememberMe(true);
		session.setRefreshTokenExpiresAt(System.currentTimeMillis() + 60_000);
		session.setRefreshTokenHash(MD5.create().digestHex16(TestConstants.REFRESH_TOKEN));
		when(redisUserSessionIndexRepository.loadUserSessionIndex(TestConstants.JTI)).thenReturn(Optional.of(session));

		AuthProfile profile = mock(AuthProfile.class);
		when(profile.getPermVersion()).thenReturn(9L);
		when(authProfileCacheApplicationService.loadCachedProfile(TestConstants.USER_ID))
			.thenReturn(Optional.of(profile));
		when(tokenService.buildTokenPair(TestConstants.USER_ID, TestConstants.JTI, 9L))
			.thenReturn(new TokenPair("new-access", "new-refresh", LocalDateTime.now().plusMinutes(5)));

		RefreshSessionResult result = newService().refreshToken(TestConstants.REFRESH_TOKEN, null);

		assertTrue(result.rememberMe());
	}

}
