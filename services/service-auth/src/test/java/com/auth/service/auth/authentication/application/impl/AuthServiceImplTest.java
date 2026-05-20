package com.auth.service.auth.authentication.application.impl;

import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.authentication.api.dto.response.UserLoginResponse;
import com.auth.service.auth.authentication.application.command.AbstractLoginCommand;
import com.auth.service.auth.authentication.application.command.UsernamePasswordLoginCommand;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogResult;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.event.LoginAuditEventPublisher;
import com.auth.service.auth.authentication.domain.model.LoginResult;
import com.auth.service.auth.authentication.domain.model.TokenPair;
import com.auth.service.auth.authentication.domain.port.LoginStrategy;
import com.auth.service.auth.authentication.infrastructure.support.AuthTokenResponseEnricher;
import com.auth.service.auth.authentication.infrastructure.token.LoginFailureRateLimiter;
import com.auth.service.auth.authentication.infrastructure.token.TokenService;
import com.auth.service.auth.authorization.infrastructure.cache.AuthProfileCache;
import com.auth.service.auth.session.infrastructure.redis.RedisUserSessionIndexRepository;
import com.auth.service.auth.session.infrastructure.redis.support.SessionLimitManager;
import com.auth.service.auth.session.infrastructure.redis.support.UserSessionIndexFactory;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
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
	private AuthTokenResponseEnricher authTokenResponseEnricher;

	@Mock
	private HttpServletRequest httpServletRequest;

	private AuthServiceImpl newService(LoginStrategy<?>... strategies) {
		return new AuthServiceImpl(List.of(strategies), authProfileCache, userSessionIndexFactory,
				redisUserSessionIndexRepository, sessionLimitManager, tokenService, loginFailureRateLimiter,
				loginAuditEventPublisher, authTokenResponseEnricher);
	}

	@Test
	@DisplayName("登录：策略精确匹配时，按编排顺序执行并返回响应")
	void login_shouldOrchestrateInOrder_whenStrategyMatchesExactly() {
		UsernamePasswordLoginCommand command = new UsernamePasswordLoginCommand("user", "pass");

		AuthProfile profile = mock(AuthProfile.class);
		when(profile.getUserId()).thenReturn(TestConstants.USER_ID);
		when(profile.getPermVersion()).thenReturn(1L);

		UserLoginResponse response = new UserLoginResponse();
		response.setId(TestConstants.USER_ID);
		response.setUsername("user");

		LoginResult loginResult = new LoginResult(response, profile, AuthLoginLogType.LOGIN_PASSWORD);

		LoginStrategy<UsernamePasswordLoginCommand> strategy = new LoginStrategy<>() {
			@Override
			public Class<UsernamePasswordLoginCommand> supports() {
				return UsernamePasswordLoginCommand.class;
			}

			@Override
			public LoginResult authenticate(UsernamePasswordLoginCommand cmd, HttpServletRequest request) {
				return loginResult;
			}
		};

		UserSessionIndex sessionIndex = new UserSessionIndex();
		sessionIndex.setUserId(TestConstants.USER_ID);
		sessionIndex.setSessionId(TestConstants.JTI);
		sessionIndex.setRefreshTokenExpiresAt(System.currentTimeMillis() + 60_000);

		when(userSessionIndexFactory.buildUserSessionIndex(any(), anyLong(), anyString(), anyString(), anyBoolean()))
			.thenReturn(sessionIndex);

		when(tokenService.buildTokenPair(anyLong(), anyString(), anyLong())).thenReturn(new TokenPair(
				TestConstants.ACCESS_TOKEN, TestConstants.REFRESH_TOKEN, LocalDateTime.now().plusMinutes(1)));

		AuthServiceImpl service = newService(strategy);

		UserLoginResponse actual = service.login(command, httpServletRequest);

		assertSame(response, actual);
		assertEquals(TestConstants.ACCESS_TOKEN, actual.getAccessToken());
		assertEquals(TestConstants.REFRESH_TOKEN, actual.getRefreshToken());
		assertNotNull(actual.getExpires());

		InOrder inOrder = inOrder(sessionLimitManager, authProfileCache, tokenService, userSessionIndexFactory,
				redisUserSessionIndexRepository, authTokenResponseEnricher, loginFailureRateLimiter,
				loginAuditEventPublisher);
		inOrder.verify(sessionLimitManager).assertSessionLimit(TestConstants.USER_ID);
		inOrder.verify(authProfileCache).createCacheAuthProfile(profile);
		inOrder.verify(tokenService).buildTokenPair(eq(TestConstants.USER_ID), anyString(), eq(1L));
		inOrder.verify(userSessionIndexFactory)
			.buildUserSessionIndex(same(httpServletRequest), eq(TestConstants.USER_ID), anyString(),
					eq(TestConstants.REFRESH_TOKEN), eq(false));
		inOrder.verify(redisUserSessionIndexRepository).createUserSessionIndex(sessionIndex);
		inOrder.verify(authTokenResponseEnricher).enrichReadMeDay(response);
		inOrder.verify(loginFailureRateLimiter).recordSuccess(TestConstants.USER_ID);
		inOrder.verify(loginAuditEventPublisher)
			.publish(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_PASSWORD), eq(AuthLoginLogResult.SUCCESS),
					eq(TestConstants.USER_ID), eq("user"), isNull(), anyString());
	}

	@Test
	@DisplayName("登录：无精确匹配时，允许父类命令类型匹配兜底")
	void login_shouldFallbackToAssignableStrategy_whenExactClassNotFound() {
		class SubLoginCommand extends UsernamePasswordLoginCommand {

			SubLoginCommand() {
				super("user", "pass");
			}

		}

		SubLoginCommand command = new SubLoginCommand();

		AuthProfile profile = mock(AuthProfile.class);
		when(profile.getUserId()).thenReturn(TestConstants.USER_ID);
		when(profile.getPermVersion()).thenReturn(1L);

		UserLoginResponse response = new UserLoginResponse();
		response.setId(TestConstants.USER_ID);

		LoginStrategy<UsernamePasswordLoginCommand> strategy = new LoginStrategy<>() {
			@Override
			public Class<UsernamePasswordLoginCommand> supports() {
				return UsernamePasswordLoginCommand.class;
			}

			@Override
			public LoginResult authenticate(UsernamePasswordLoginCommand cmd, HttpServletRequest request) {
				return new LoginResult(response, profile, AuthLoginLogType.LOGIN_PASSWORD);
			}
		};

		when(userSessionIndexFactory.buildUserSessionIndex(any(), anyLong(), anyString(), anyString(), anyBoolean()))
			.thenReturn(new UserSessionIndex());
		when(tokenService.buildTokenPair(anyLong(), anyString(), anyLong())).thenReturn(new TokenPair(
				TestConstants.ACCESS_TOKEN, TestConstants.REFRESH_TOKEN, LocalDateTime.now().plusMinutes(1)));

		AuthServiceImpl service = newService(strategy);

		UserLoginResponse actual = service.login(command, httpServletRequest);

		assertSame(response, actual);
		verify(sessionLimitManager).assertSessionLimit(TestConstants.USER_ID);
	}

	@Test
	@DisplayName("登录：rememberMe=true 时写入会话索引")
	void login_shouldPassRememberMe_whenRequestRememberMeTrue() {
		UsernamePasswordLoginCommand command = new UsernamePasswordLoginCommand("user", "pass");
		command.setRememberMe(true);

		AuthProfile profile = mock(AuthProfile.class);
		when(profile.getUserId()).thenReturn(TestConstants.USER_ID);
		when(profile.getPermVersion()).thenReturn(1L);

		UserLoginResponse response = new UserLoginResponse();
		LoginStrategy<UsernamePasswordLoginCommand> strategy = new LoginStrategy<>() {
			@Override
			public Class<UsernamePasswordLoginCommand> supports() {
				return UsernamePasswordLoginCommand.class;
			}

			@Override
			public LoginResult authenticate(UsernamePasswordLoginCommand cmd, HttpServletRequest request) {
				return new LoginResult(response, profile, AuthLoginLogType.LOGIN_PASSWORD);
			}
		};

		when(userSessionIndexFactory.buildUserSessionIndex(any(), anyLong(), anyString(), anyString(), eq(true)))
			.thenReturn(new UserSessionIndex());
		when(tokenService.buildTokenPair(anyLong(), anyString(), anyLong())).thenReturn(new TokenPair(
				TestConstants.ACCESS_TOKEN, TestConstants.REFRESH_TOKEN, LocalDateTime.now().plusMinutes(1)));

		newService(strategy).login(command, httpServletRequest);

		verify(userSessionIndexFactory).buildUserSessionIndex(same(httpServletRequest), eq(TestConstants.USER_ID),
				anyString(), anyString(), eq(true));
	}

	@Test
	@DisplayName("登录：无任何策略时，抛出 AUTH_TYPE_UNSUPPORTED 并写审计")
	void login_shouldThrowAuthTypeUnsupported_whenNoStrategy() {
		AbstractLoginCommand unknown = new AbstractLoginCommand() {
			@Override
			public AuthLoginLogType loginLogType() {
				return AuthLoginLogType.LOGIN_PASSWORD;
			}

			@Override
			public String principalForAudit() {
				return null;
			}
		};

		AuthServiceImpl service = newService();

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> service.login(unknown, httpServletRequest));

		assertEquals(AuthResultCode.AUTH_TYPE_UNSUPPORTED, exception.getResultCode());
		verify(loginAuditEventPublisher).publish(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_PASSWORD),
				eq(AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR), isNull(), isNull(), eq(exception.getMessageKey()),
				isNull());
	}

	@Test
	@DisplayName("登录：编排失败时回滚已写入的画像与会话")
	void login_shouldRollbackArtifacts_whenOrchestrationFailsAfterProfileCached() {
		UsernamePasswordLoginCommand command = new UsernamePasswordLoginCommand("user", "pass");

		AuthProfile profile = mock(AuthProfile.class);
		when(profile.getUserId()).thenReturn(TestConstants.USER_ID);
		when(profile.getPermVersion()).thenReturn(1L);

		UserLoginResponse response = new UserLoginResponse();
		response.setId(TestConstants.USER_ID);
		response.setUsername("user");

		LoginResult loginResult = new LoginResult(response, profile, AuthLoginLogType.LOGIN_PASSWORD);

		LoginStrategy<UsernamePasswordLoginCommand> strategy = new LoginStrategy<>() {
			@Override
			public Class<UsernamePasswordLoginCommand> supports() {
				return UsernamePasswordLoginCommand.class;
			}

			@Override
			public LoginResult authenticate(UsernamePasswordLoginCommand cmd, HttpServletRequest request) {
				return loginResult;
			}
		};

		when(tokenService.buildTokenPair(anyLong(), anyString(), anyLong()))
			.thenThrow(new AuthBusinessException(AuthResultCode.SERVER_ERROR));

		AuthServiceImpl service = newService(strategy);

		assertThrows(AuthBusinessException.class, () -> service.login(command, httpServletRequest));

		verify(authProfileCache).createCacheAuthProfile(profile);
		verify(authProfileCache).evictCacheAuthProfile(TestConstants.USER_ID);
		verify(redisUserSessionIndexRepository, never()).createUserSessionIndex(any());
	}

	@Test
	@DisplayName("登出：token 为空或无法解析时不删除会话")
	void logout_shouldSkipRevoke_whenTokenBlankOrUnparsed() {
		AuthServiceImpl service = newService();

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
		AuthServiceImpl service = newService();

		JwtUserToken jwtUserToken = mock(JwtUserToken.class);
		when(jwtUserToken.getJti()).thenReturn(TestConstants.JTI);
		when(jwtUserToken.getUserId()).thenReturn(TestConstants.USER_ID);

		when(tokenService.parseAccessTokenSafe(TestConstants.ACCESS_TOKEN)).thenReturn(Optional.of(jwtUserToken));

		service.logout(TestConstants.ACCESS_TOKEN, null);

		verify(redisUserSessionIndexRepository).deleteSession(TestConstants.JTI);
		verify(redisUserSessionIndexRepository).removeActiveSession(TestConstants.USER_ID, TestConstants.JTI);
		verify(loginAuditEventPublisher).publish(isNull(), eq(AuthLoginLogType.LOGOUT), eq(AuthLoginLogResult.SUCCESS),
				eq(TestConstants.USER_ID), isNull(), isNull(), eq(TestConstants.JTI));
	}

}
