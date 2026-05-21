package com.auth.service.auth.authentication.application;

import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.authentication.api.dto.response.UserLoginResponse;
import com.auth.service.auth.authentication.application.command.AbstractLoginCommand;
import com.auth.service.auth.authentication.application.command.UsernamePasswordLoginCommand;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditCoordinator;
import com.auth.service.auth.authentication.application.port.AuthProfileCachePort;
import com.auth.service.auth.authentication.application.port.LoginStrategy;
import com.auth.service.auth.authentication.application.port.SessionLimitPort;
import com.auth.service.auth.authentication.application.port.TokenIssuerPort;
import com.auth.service.auth.authentication.application.port.RememberMePolicyPort;
import com.auth.service.auth.authentication.application.port.UserSessionIndexBuildPort;
import com.auth.service.auth.authentication.application.port.UserSessionIndexPort;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.model.AuthenticatedUser;
import com.auth.service.auth.authentication.domain.model.LoginResult;
import com.auth.service.auth.authentication.domain.model.TokenPair;
import com.auth.service.auth.authentication.domain.port.LoginFailureRateLimitPort;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * LoginApplicationService 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class LoginApplicationServiceTest {

	@Mock
	private AuthProfileCachePort authProfileCachePort;

	@Mock
	private UserSessionIndexBuildPort userSessionIndexBuildPort;

	@Mock
	private UserSessionIndexPort userSessionIndexPort;

	@Mock
	private SessionLimitPort sessionLimitPort;

	@Mock
	private TokenIssuerPort tokenIssuerPort;

	@Mock
	private LoginFailureRateLimitPort loginFailureRateLimitPort;

	@Mock
	private LoginAuditCoordinator loginAuditCoordinator;

	@Mock
	private RememberMePolicyPort rememberMePolicyPort;

	@Mock
	private HttpServletRequest httpServletRequest;

	private static LoginResult toLoginResult(UserLoginResponse response, AuthProfile profile) {
		AuthenticatedUser user = new AuthenticatedUser(response.getId(), response.getUsername(), response.getNickname(),
				response.getAvatar());
		return new LoginResult(user, profile, AuthLoginLogType.LOGIN_PASSWORD);
	}

	private LoginApplicationService newService(LoginStrategy<?>... strategies) {
		return new LoginApplicationService(List.of(strategies), authProfileCachePort, userSessionIndexBuildPort,
				userSessionIndexPort, sessionLimitPort, tokenIssuerPort, loginFailureRateLimitPort,
				loginAuditCoordinator, rememberMePolicyPort);
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

		LoginResult loginResult = toLoginResult(response, profile);

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

		when(userSessionIndexBuildPort.build(any(), anyLong(), anyString(), anyString(), anyBoolean()))
			.thenReturn(sessionIndex);

		when(tokenIssuerPort.buildTokenPair(anyLong(), anyString(), anyLong())).thenReturn(new TokenPair(
				TestConstants.ACCESS_TOKEN, TestConstants.REFRESH_TOKEN, LocalDateTime.now().plusMinutes(1)));
		when(rememberMePolicyPort.resolveReadMeDay()).thenReturn(7L);

		LoginApplicationService service = newService(strategy);

		UserLoginResponse actual = service.login(command, httpServletRequest);

		assertEquals(TestConstants.USER_ID, actual.getId());
		assertEquals("user", actual.getUsername());
		assertEquals(TestConstants.ACCESS_TOKEN, actual.getAccessToken());
		assertEquals(TestConstants.REFRESH_TOKEN, actual.getRefreshToken());
		assertNotNull(actual.getExpires());
		assertEquals(7L, actual.getReadMeDay());

		InOrder inOrder = inOrder(sessionLimitPort, authProfileCachePort, tokenIssuerPort, userSessionIndexBuildPort,
				userSessionIndexPort, rememberMePolicyPort, loginFailureRateLimitPort, loginAuditCoordinator);
		inOrder.verify(sessionLimitPort).assertSessionLimit(TestConstants.USER_ID);
		inOrder.verify(authProfileCachePort).cacheProfile(profile);
		inOrder.verify(tokenIssuerPort).buildTokenPair(eq(TestConstants.USER_ID), anyString(), eq(1L));
		inOrder.verify(userSessionIndexBuildPort)
			.build(same(httpServletRequest), eq(TestConstants.USER_ID), anyString(), eq(TestConstants.REFRESH_TOKEN),
					eq(false));
		inOrder.verify(userSessionIndexPort).createUserSessionIndex(sessionIndex);
		inOrder.verify(rememberMePolicyPort).resolveReadMeDay();
		inOrder.verify(loginFailureRateLimitPort).recordSuccess(TestConstants.USER_ID);
		inOrder.verify(loginAuditCoordinator)
			.auditLoginSuccess(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_PASSWORD), eq(TestConstants.USER_ID),
					eq("user"), anyString());
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
				return toLoginResult(response, profile);
			}
		};

		when(userSessionIndexBuildPort.build(any(), anyLong(), anyString(), anyString(), anyBoolean()))
			.thenReturn(new UserSessionIndex());
		when(tokenIssuerPort.buildTokenPair(anyLong(), anyString(), anyLong())).thenReturn(new TokenPair(
				TestConstants.ACCESS_TOKEN, TestConstants.REFRESH_TOKEN, LocalDateTime.now().plusMinutes(1)));

		LoginApplicationService service = newService(strategy);

		UserLoginResponse actual = service.login(command, httpServletRequest);

		assertEquals(TestConstants.USER_ID, actual.getId());
		verify(sessionLimitPort).assertSessionLimit(TestConstants.USER_ID);
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
				return toLoginResult(response, profile);
			}
		};

		when(userSessionIndexBuildPort.build(any(), anyLong(), anyString(), anyString(), eq(true)))
			.thenReturn(new UserSessionIndex());
		when(tokenIssuerPort.buildTokenPair(anyLong(), anyString(), anyLong())).thenReturn(new TokenPair(
				TestConstants.ACCESS_TOKEN, TestConstants.REFRESH_TOKEN, LocalDateTime.now().plusMinutes(1)));

		newService(strategy).login(command, httpServletRequest);

		verify(userSessionIndexBuildPort).build(same(httpServletRequest), eq(TestConstants.USER_ID), anyString(),
				anyString(), eq(true));
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

		LoginApplicationService service = newService();

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> service.login(unknown, httpServletRequest));

		assertEquals(AuthResultCode.AUTH_TYPE_UNSUPPORTED, exception.getResultCode());
		verify(loginAuditCoordinator).auditUnsupportedLoginCommand(same(httpServletRequest), eq(unknown),
				eq(exception.getMessageKey()));
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

		LoginResult loginResult = toLoginResult(response, profile);

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

		when(tokenIssuerPort.buildTokenPair(anyLong(), anyString(), anyLong()))
			.thenThrow(new AuthBusinessException(AuthResultCode.SERVER_ERROR));

		LoginApplicationService service = newService(strategy);

		assertThrows(AuthBusinessException.class, () -> service.login(command, httpServletRequest));

		verify(authProfileCachePort).cacheProfile(profile);
		verify(authProfileCachePort).evictProfile(TestConstants.USER_ID);
		verify(userSessionIndexPort, never()).createUserSessionIndex(any());
	}

	@Test
	@DisplayName("登出：token 为空或无法解析时不删除会话")
	void logout_shouldSkipRevoke_whenTokenBlankOrUnparsed() {
		LoginApplicationService service = newService();

		service.logout(" ", null);
		verify(tokenIssuerPort).parseAccessTokenSafe(" ");
		verifyNoInteractions(userSessionIndexPort);

		reset(tokenIssuerPort, userSessionIndexPort);
		when(tokenIssuerPort.parseAccessTokenSafe("x")).thenReturn(Optional.empty());
		service.logout("x", null);
		verify(tokenIssuerPort).parseAccessTokenSafe("x");
		verifyNoInteractions(userSessionIndexPort);
	}

	@Test
	@DisplayName("登出：token 有效时，删除会话并从活跃集合移除")
	void logout_shouldDeleteSessionAndRemoveActiveSession_whenTokenValid() {
		LoginApplicationService service = newService();

		JwtUserToken jwtUserToken = mock(JwtUserToken.class);
		when(jwtUserToken.getJti()).thenReturn(TestConstants.JTI);
		when(jwtUserToken.getUserId()).thenReturn(TestConstants.USER_ID);

		when(tokenIssuerPort.parseAccessTokenSafe(TestConstants.ACCESS_TOKEN)).thenReturn(Optional.of(jwtUserToken));

		var hint = service.logout(TestConstants.ACCESS_TOKEN, null);

		verify(userSessionIndexPort).deleteSession(TestConstants.JTI);
		verify(userSessionIndexPort).removeActiveSession(TestConstants.USER_ID, TestConstants.JTI);
		verifyNoInteractions(loginAuditCoordinator);
		assertTrue(hint.isPresent());
		assertEquals(TestConstants.USER_ID, hint.get().userId());
		assertEquals(TestConstants.JTI, hint.get().jti());
	}

}
