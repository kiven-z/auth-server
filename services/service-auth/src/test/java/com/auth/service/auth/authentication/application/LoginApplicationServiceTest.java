package com.auth.service.auth.authentication.application;

import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.authentication.api.dto.response.UserLoginResponse;
import com.auth.service.auth.authentication.application.input.credential.AbstractCredentialLogin;
import com.auth.service.auth.authentication.application.input.credential.UsernamePasswordLogin;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditService;
import com.auth.service.auth.authentication.application.port.LoginStrategy;
import com.auth.service.auth.authentication.application.port.TokenIssuerPort;
import com.auth.service.auth.authentication.application.port.UserSessionIndexPort;
import com.auth.service.auth.authentication.application.session.LoginSessionOrchestrator;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.model.AuthenticatedUser;
import com.auth.service.auth.authentication.domain.model.LoginResult;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link LoginApplicationService} 单元测试：策略路由、会话签发委托与登出。
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class LoginApplicationServiceTest {

	@Mock
	private LoginSessionOrchestrator loginSessionOrchestrator;

	@Mock
	private UserSessionIndexPort userSessionIndexPort;

	@Mock
	private TokenIssuerPort tokenIssuerPort;

	@Mock
	private LoginAuditService loginAuditService;

	@Mock
	private HttpServletRequest httpServletRequest;

	private static LoginResult toLoginResult(UserLoginResponse response, AuthProfile profile) {
		AuthenticatedUser user = new AuthenticatedUser(response.getId(), response.getUsername(), response.getNickname(),
				response.getAvatar());
		return new LoginResult(user, profile, AuthLoginLogType.LOGIN_PASSWORD);
	}

	private LoginApplicationService newService(LoginStrategy<?>... strategies) {
		return new LoginApplicationService(List.of(strategies), loginSessionOrchestrator, userSessionIndexPort,
				tokenIssuerPort, loginAuditService);
	}

	@Test
	@DisplayName("登录：策略认证成功后委托 LoginSessionOrchestrator 签发会话")
	void login_shouldDelegateToSessionOrchestrator_whenStrategyAuthenticates() {
		UsernamePasswordLogin credential = new UsernamePasswordLogin();
		credential.setUsername("user");
		credential.setPassword("pass");
		credential.setRememberMe(true);

		AuthProfile profile = mock(AuthProfile.class);
		UserLoginResponse response = new UserLoginResponse();
		response.setId(TestConstants.USER_ID);
		response.setUsername("user");
		LoginResult loginResult = toLoginResult(response, profile);

		LoginStrategy<UsernamePasswordLogin> strategy = new LoginStrategy<>() {
			@Override
			public Class<UsernamePasswordLogin> supports() {
				return UsernamePasswordLogin.class;
			}

			@Override
			public LoginResult authenticate(UsernamePasswordLogin cmd, HttpServletRequest request) {
				return loginResult;
			}
		};

		UserLoginResponse issued = new UserLoginResponse();
		issued.setAccessToken(TestConstants.ACCESS_TOKEN);
		when(loginSessionOrchestrator.issueSession(eq(loginResult), any(UserLoginResponse.class), eq(true),
				same(httpServletRequest)))
			.thenReturn(issued);

		UserLoginResponse actual = newService(strategy).login(credential, httpServletRequest);

		assertSame(issued, actual);
		verify(loginSessionOrchestrator).issueSession(eq(loginResult), any(UserLoginResponse.class), eq(true),
				same(httpServletRequest));
	}

	@Test
	@DisplayName("登录：无精确匹配时，允许父类命令类型匹配兜底")
	void login_shouldFallbackToAssignableStrategy_whenExactClassNotFound() {
		class SubUsernamePasswordLogin extends UsernamePasswordLogin {

			SubUsernamePasswordLogin() {
				setUsername("user");
				setPassword("pass");
			}

		}

		SubUsernamePasswordLogin credential = new SubUsernamePasswordLogin();
		AuthProfile profile = mock(AuthProfile.class);
		UserLoginResponse response = new UserLoginResponse();
		response.setId(TestConstants.USER_ID);
		LoginResult loginResult = toLoginResult(response, profile);

		LoginStrategy<UsernamePasswordLogin> strategy = new LoginStrategy<>() {
			@Override
			public Class<UsernamePasswordLogin> supports() {
				return UsernamePasswordLogin.class;
			}

			@Override
			public LoginResult authenticate(UsernamePasswordLogin cmd, HttpServletRequest request) {
				return loginResult;
			}
		};

		when(loginSessionOrchestrator.issueSession(eq(loginResult), any(UserLoginResponse.class), eq(false),
				same(httpServletRequest)))
			.thenReturn(response);

		UserLoginResponse actual = newService(strategy).login(credential, httpServletRequest);

		assertEquals(TestConstants.USER_ID, actual.getId());
		verify(loginSessionOrchestrator).issueSession(eq(loginResult), any(UserLoginResponse.class), eq(false),
				same(httpServletRequest));
	}

	@Test
	@DisplayName("登录：无任何策略时，抛出 AUTH_TYPE_UNSUPPORTED 并写审计")
	void login_shouldThrowAuthTypeUnsupported_whenNoStrategy() {
		AbstractCredentialLogin unknown = new AbstractCredentialLogin() {
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
		verify(loginAuditService).auditUnsupportedLoginCommand(same(httpServletRequest), eq(unknown),
				eq(exception.getMessageKey()));
		verifyNoInteractions(loginSessionOrchestrator);
	}

	@Test
	@DisplayName("登出：token 为空或无法解析时不删除会话")
	void logout_shouldSkipRevoke_whenTokenBlankOrUnparsed() {
		LoginApplicationService service = newService();

		service.logout(" ");
		verify(tokenIssuerPort).parseAccessTokenSafe(" ");
		verifyNoInteractions(userSessionIndexPort);

		reset(tokenIssuerPort, userSessionIndexPort);
		when(tokenIssuerPort.parseAccessTokenSafe("x")).thenReturn(Optional.empty());
		service.logout("x");
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

		var hint = service.logout(TestConstants.ACCESS_TOKEN);

		verify(userSessionIndexPort).deleteSession(TestConstants.JTI);
		verify(userSessionIndexPort).removeActiveSession(TestConstants.USER_ID, TestConstants.JTI);
		verifyNoInteractions(loginAuditService);
		assertTrue(hint.isPresent());
		assertEquals(TestConstants.USER_ID, hint.get().userId());
		assertEquals(TestConstants.JTI, hint.get().jti());
	}

}
