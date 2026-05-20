package com.auth.service.auth.authentication.application.login.strategy;

import com.auth.service.auth.authentication.application.command.UsernamePasswordLoginCommand;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditCoordinator;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.enums.CredentialDimension;
import com.auth.service.auth.authentication.domain.model.LoginAccount;
import com.auth.service.auth.authentication.domain.port.LoginAccountLookupPort;
import com.auth.service.auth.authentication.domain.port.LoginFailureRateLimitPort;
import com.auth.service.auth.authorization.application.AuthProfileQueryService;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link UsernamePasswordLoginStrategy} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class UsernamePasswordLoginStrategyTest {

	@Mock
	private LoginAccountLookupPort loginAccountLookupPort;

	@Mock
	private LoginFailureRateLimitPort loginFailureRateLimitPort;

	@Mock
	private AuthProfileQueryService authProfileQueryService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private LoginAuditCoordinator loginAuditCoordinator;

	@Mock
	private HttpServletRequest httpServletRequest;

	@Test
	@DisplayName("用户不存在时抛出 USERNAME_OR_PASSWORD_ERROR 并记录审计")
	void resolveUser_shouldThrow_whenUserNotFound() {
		when(loginAccountLookupPort.findForLogin(CredentialDimension.USERNAME, "u")).thenReturn(Optional.empty());
		UsernamePasswordLoginStrategy strategy = new UsernamePasswordLoginStrategy(loginAccountLookupPort,
				loginFailureRateLimitPort, authProfileQueryService, passwordEncoder, loginAuditCoordinator);

		UsernamePasswordLoginCommand command = new UsernamePasswordLoginCommand("u", "p");

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(command, httpServletRequest));

		assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR, exception.getResultCode());
		verify(loginAuditCoordinator).auditUserUnknownAfterResolve(same(httpServletRequest),
				eq(AuthLoginLogType.LOGIN_PASSWORD), eq("u"), eq(exception.getMessageKey()));
	}

	@Test
	@DisplayName("账号禁用时抛出 USER_DISABLED 并记录审计")
	void authenticate_shouldThrowUserDisabled_whenAccountDisabled() {
		LoginAccount account = new LoginAccount(1L, "u", 0, "encoded", null, null, 1L);
		when(loginAccountLookupPort.findForLogin(CredentialDimension.USERNAME, "u")).thenReturn(Optional.of(account));

		UsernamePasswordLoginStrategy strategy = new UsernamePasswordLoginStrategy(loginAccountLookupPort,
				loginFailureRateLimitPort, authProfileQueryService, passwordEncoder, loginAuditCoordinator);

		UsernamePasswordLoginCommand command = new UsernamePasswordLoginCommand("u", "p");

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(command, httpServletRequest));

		assertEquals(AuthResultCode.USER_DISABLED, exception.getResultCode());
		verify(loginAuditCoordinator).auditAccountDisabled(same(httpServletRequest),
				eq(AuthLoginLogType.LOGIN_PASSWORD), eq(1L), eq("u"), eq(AuthResultCode.USER_DISABLED.getMessageKey()));
		verifyNoInteractions(passwordEncoder);
	}

	@Test
	@DisplayName("supports 返回 UsernamePasswordLoginCommand.class")
	void supports_shouldReturnUsernamePasswordLoginCommandClass() {
		UsernamePasswordLoginStrategy strategy = new UsernamePasswordLoginStrategy(loginAccountLookupPort,
				loginFailureRateLimitPort, authProfileQueryService, passwordEncoder, loginAuditCoordinator);

		assertEquals(UsernamePasswordLoginCommand.class, strategy.supports());
	}

	@Test
	@DisplayName("密码错误时抛出 USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING")
	void authenticate_shouldThrowRemaining_whenPasswordMismatch() {
		LoginAccount account = new LoginAccount(1L, "u", 1, "encoded", null, null, 1L);
		when(loginAccountLookupPort.findForLogin(CredentialDimension.USERNAME, "u")).thenReturn(Optional.of(account));

		when(passwordEncoder.matches("p", "encoded")).thenReturn(false);
		doNothing().when(loginFailureRateLimitPort).assertNotLocked(1L);
		when(loginFailureRateLimitPort.recordFailure(1L)).thenReturn(2);

		UsernamePasswordLoginStrategy strategy = new UsernamePasswordLoginStrategy(loginAccountLookupPort,
				loginFailureRateLimitPort, authProfileQueryService, passwordEncoder, loginAuditCoordinator);

		UsernamePasswordLoginCommand command = new UsernamePasswordLoginCommand("u", "p");

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(command, httpServletRequest));

		assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING, exception.getResultCode());
		verify(loginAuditCoordinator).auditBadCredentialsWithRemaining(same(httpServletRequest),
				eq(AuthLoginLogType.LOGIN_PASSWORD), eq(1L), eq("u"), eq(2));
	}

}
