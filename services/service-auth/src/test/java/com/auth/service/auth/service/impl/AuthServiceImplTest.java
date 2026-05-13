package com.auth.service.auth.service.impl;

import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.model.enums.AuthResultCode;
import com.auth.service.auth.infrastructure.login.LoginFailureRateLimiter;
import com.auth.service.auth.infrastructure.login.LoginResult;
import com.auth.service.auth.infrastructure.login.TokenPair;
import com.auth.service.auth.infrastructure.login.TokenService;
import com.auth.service.auth.infrastructure.login.strategy.LoginStrategy;
import com.auth.service.auth.infrastructure.profile.AuthProfileCache;
import com.auth.service.auth.infrastructure.session.SessionLimitManager;
import com.auth.service.auth.infrastructure.session.UserSessionIndexFactory;
import com.auth.service.auth.infrastructure.session.redis.RedisUserSessionIndexRepository;
import com.auth.service.auth.model.enums.AuthLoginLogResult;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.request.login.AbstractLoginRequest;
import com.auth.service.auth.model.request.login.UsernamePasswordLoginRequest;
import com.auth.service.auth.model.response.UserLoginResponse;
import com.auth.service.auth.infrastructure.login.audit.LoginAuditEventPublisher;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthServiceImpl 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

	@Mock
	private AuthProfileCache authProfileCache;

	@Mock
	private UserSessionIndexFactory userSessionIndexFactory;

	@Mock
	private RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	@Mock
	private SessionLimitManager sessionLimitManager;

	@Mock
	private TokenService tokenService;

	@Mock
	private LoginFailureRateLimiter loginFailureRateLimiter;

	@Mock
	private LoginAuditEventPublisher loginAuditEventPublisher;

	@Mock
	private HttpServletRequest httpServletRequest;

	@Test
	@DisplayName("登录：策略精确匹配时，按编排顺序执行并返回响应")
	void login_shouldOrchestrateInOrder_whenStrategyMatchesExactly() {
		// Arrange
		UsernamePasswordLoginRequest auth = new UsernamePasswordLoginRequest();
		auth.setUsername("user");
		auth.setPassword("pass");

		AuthProfile profile = mock(AuthProfile.class);
		when(profile.getUserId()).thenReturn(TestConstants.USER_ID);
		when(profile.getPermVersion()).thenReturn(1L);

		UserLoginResponse response = new UserLoginResponse();
		response.setId(TestConstants.USER_ID);
		response.setUsername("user");

		LoginResult loginResult = new LoginResult(response, profile, AuthLoginLogType.LOGIN_PASSWORD);

		LoginStrategy<UsernamePasswordLoginRequest> strategy = new LoginStrategy<>() {
			@Override
			public Class<UsernamePasswordLoginRequest> supports() {
				return UsernamePasswordLoginRequest.class;
			}

			@Override
			public LoginResult authenticate(UsernamePasswordLoginRequest requestAuth, HttpServletRequest request) {
				return loginResult;
			}
		};

		UserSessionIndex sessionIndex = new UserSessionIndex();
		sessionIndex.setUserId(TestConstants.USER_ID);
		sessionIndex.setSessionId(TestConstants.JTI);
		sessionIndex.setRefreshTokenExpiresAt(System.currentTimeMillis() + 60_000);

		when(userSessionIndexFactory.buildUserSessionIndex(any(), anyLong(), anyString(), anyString()))
			.thenReturn(sessionIndex);

		when(tokenService.buildTokenPair(anyLong(), anyString(), anyLong())).thenReturn(new TokenPair(
				TestConstants.ACCESS_TOKEN, TestConstants.REFRESH_TOKEN, LocalDateTime.now().plusMinutes(5)));

		AuthServiceImpl service = new AuthServiceImpl(List.of(strategy), authProfileCache, userSessionIndexFactory,
				redisUserSessionIndexRepository, sessionLimitManager, tokenService, loginFailureRateLimiter,
				loginAuditEventPublisher);

		// Act
		UserLoginResponse actual = service.login(auth, httpServletRequest);

		// Assert
		assertSame(response, actual);
		assertEquals(TestConstants.ACCESS_TOKEN, actual.getAccessToken());
		assertEquals(TestConstants.REFRESH_TOKEN, actual.getRefreshToken());
		assertNotNull(actual.getExpires());

		InOrder inOrder = inOrder(sessionLimitManager, authProfileCache, tokenService, userSessionIndexFactory,
				redisUserSessionIndexRepository, loginFailureRateLimiter, loginAuditEventPublisher);
		inOrder.verify(sessionLimitManager).assertSessionLimit(TestConstants.USER_ID);
		inOrder.verify(authProfileCache).createCacheAuthProfile(profile);
		inOrder.verify(tokenService).buildTokenPair(eq(TestConstants.USER_ID), anyString(), eq(1L));
		inOrder.verify(userSessionIndexFactory)
			.buildUserSessionIndex(same(httpServletRequest), eq(TestConstants.USER_ID), anyString(),
					eq(TestConstants.REFRESH_TOKEN));
		inOrder.verify(redisUserSessionIndexRepository).createUserSessionIndex(sessionIndex);
		inOrder.verify(loginFailureRateLimiter).recordSuccess(TestConstants.USER_ID);
		inOrder.verify(loginAuditEventPublisher)
			.publish(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_PASSWORD),
					eq(AuthLoginLogResult.SUCCESS), eq(TestConstants.USER_ID), eq("user"), isNull(), anyString());
	}

	@Test
	@DisplayName("登录：无精确匹配时，允许父类/接口匹配兜底")
	void login_shouldFallbackToAssignableStrategy_whenExactClassNotFound() {
		// Arrange
		class MyLoginRequest extends UsernamePasswordLoginRequest {

		}

		MyLoginRequest auth = new MyLoginRequest();
		auth.setUsername("user");
		auth.setPassword("pass");

		AuthProfile profile = mock(AuthProfile.class);
		when(profile.getUserId()).thenReturn(TestConstants.USER_ID);
		when(profile.getPermVersion()).thenReturn(1L);

		UserLoginResponse response = new UserLoginResponse();
		response.setId(TestConstants.USER_ID);

		LoginStrategy<UsernamePasswordLoginRequest> strategy = new LoginStrategy<>() {
			@Override
			public Class<UsernamePasswordLoginRequest> supports() {
				return UsernamePasswordLoginRequest.class;
			}

			@Override
			public LoginResult authenticate(UsernamePasswordLoginRequest requestAuth, HttpServletRequest request) {
				return new LoginResult(response, profile, AuthLoginLogType.LOGIN_PASSWORD);
			}
		};

		when(userSessionIndexFactory.buildUserSessionIndex(any(), anyLong(), anyString(), anyString()))
			.thenReturn(new UserSessionIndex());
		when(tokenService.buildTokenPair(anyLong(), anyString(), anyLong())).thenReturn(new TokenPair(
				TestConstants.ACCESS_TOKEN, TestConstants.REFRESH_TOKEN, LocalDateTime.now().plusMinutes(1)));

		AuthServiceImpl service = new AuthServiceImpl(List.of(strategy), authProfileCache, userSessionIndexFactory,
				redisUserSessionIndexRepository, sessionLimitManager, tokenService, loginFailureRateLimiter,
				loginAuditEventPublisher);

		// Act
		UserLoginResponse actual = service.login(auth, httpServletRequest);

		// Assert
		assertSame(response, actual);
		verify(sessionLimitManager).assertSessionLimit(TestConstants.USER_ID);
	}

	@Test
	@DisplayName("登录：无任何策略时，抛出 AUTH_TYPE_UNSUPPORTED 并写审计")
	void login_shouldThrowAuthTypeUnsupported_whenNoStrategy() {
		// Arrange
		AbstractLoginRequest auth = new AbstractLoginRequest() {
		};

		AuthServiceImpl service = new AuthServiceImpl(List.of(), authProfileCache, userSessionIndexFactory,
				redisUserSessionIndexRepository, sessionLimitManager, tokenService, loginFailureRateLimiter,
				loginAuditEventPublisher);

		// Act
		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> service.login(auth, httpServletRequest));

		// Assert
		assertNotNull(exception.getResultCode());
		assertEquals(AuthResultCode.AUTH_TYPE_UNSUPPORTED, exception.getResultCode());
		verify(loginAuditEventPublisher).publish(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_PASSWORD),
				eq(AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR), isNull(), isNull(), eq(exception.getMessageKey()),
				isNull());
	}

	@Test
	@DisplayName("登出：token 为空/解析异常/kind 不匹配时，静默返回不做任何删除")
	void logout_shouldReturnSilently_whenTokenBlankOrParseThrowsOrKindNotExternalAccess() {
		// Arrange（logout 不使用策略表，传空列表即可）
		AuthServiceImpl service = new AuthServiceImpl(List.of(), authProfileCache, userSessionIndexFactory,
				redisUserSessionIndexRepository, sessionLimitManager, tokenService, loginFailureRateLimiter,
				loginAuditEventPublisher);

		service.logout(" ", null);
		verify(tokenService).parseAccessTokenSafe(" ");
		verifyNoInteractions(redisUserSessionIndexRepository);

		reset(tokenService, redisUserSessionIndexRepository);
		when(tokenService.parseAccessTokenSafe("x")).thenReturn(Optional.empty());
		service.logout("x", null);
		verify(tokenService).parseAccessTokenSafe("x");
		verifyNoInteractions(redisUserSessionIndexRepository);
	}

	@Test
	@DisplayName("登出：token 有效时，删除会话并从活跃集合移除")
	void logout_shouldDeleteSessionAndRemoveActiveSession_whenTokenValid() {
		// Arrange
		AuthServiceImpl service = new AuthServiceImpl(List.of(), authProfileCache, userSessionIndexFactory,
				redisUserSessionIndexRepository, sessionLimitManager, tokenService, loginFailureRateLimiter,
				loginAuditEventPublisher);

		JwtUserToken jwtUserToken = mock(JwtUserToken.class);
		when(jwtUserToken.getJti()).thenReturn(TestConstants.JTI);
		when(jwtUserToken.getUserId()).thenReturn(TestConstants.USER_ID);

		when(tokenService.parseAccessTokenSafe(TestConstants.ACCESS_TOKEN)).thenReturn(Optional.of(jwtUserToken));

		// Act
		service.logout(TestConstants.ACCESS_TOKEN, null);

		// Assert
		verify(redisUserSessionIndexRepository).deleteSession(TestConstants.JTI);
		verify(redisUserSessionIndexRepository).removeActiveSession(TestConstants.USER_ID, TestConstants.JTI);
		verify(loginAuditEventPublisher).publish(isNull(), eq(AuthLoginLogType.LOGOUT), eq(AuthLoginLogResult.SUCCESS),
				eq(TestConstants.USER_ID), isNull(), isNull(), eq(TestConstants.JTI));
	}

}
