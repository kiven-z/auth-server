package com.auth.service.auth.application.login.strategy;

import com.auth.service.auth.application.login.LoginAuditService;
import com.auth.service.auth.application.login.input.UsernamePasswordCommand;
import com.auth.service.auth.application.port.authorization.AuthProfileQueryPort;
import com.auth.service.auth.application.port.login.CredentialPort;
import com.auth.service.auth.domain.authentication.model.LoginAccount;
import com.auth.service.auth.domain.authentication.model.enums.AuthLoginLogType;
import com.auth.service.auth.domain.authentication.model.enums.CredentialDimension;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.AuthResultCode;
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
import static org.mockito.Mockito.*;

/**
 * {@link UsernamePasswordLoginStrategy} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class UsernamePasswordLoginStrategyTest {

	@Mock
	private CredentialPort credentialGateway;

	@Mock
	private AuthProfileQueryPort authProfileQueryPort;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private LoginAuditService loginAuditService;

	@Mock
	private HttpServletRequest httpServletRequest;

	@Test
	@DisplayName("用户不存在时抛出 USERNAME_OR_PASSWORD_ERROR 并记录审计")
	void resolveUser_shouldThrow_whenUserNotFound() {
		when(credentialGateway.findForLogin(CredentialDimension.USERNAME, "u")).thenReturn(Optional.empty());
		UsernamePasswordLoginStrategy strategy = new UsernamePasswordLoginStrategy(credentialGateway,
				authProfileQueryPort, passwordEncoder, loginAuditService);

		UsernamePasswordCommand command = new UsernamePasswordCommand();
		command.setUsername("u");
		command.setPassword("p");
		when(loginAuditService.resolvePrincipal(command)).thenReturn("u");

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(command, httpServletRequest));

		assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR, exception.getResultCode());
		verify(loginAuditService).resolvePrincipal(command);
		verify(loginAuditService).auditUserUnknownAfterResolve(same(httpServletRequest),
				eq(AuthLoginLogType.LOGIN_PASSWORD), eq("u"), eq(exception.getMessageKey()));
	}

	@Test
	@DisplayName("账号禁用时抛出 USER_DISABLED 并记录审计")
	void authenticate_shouldThrowUserDisabled_whenAccountDisabled() {
		LoginAccount account = new LoginAccount(1L, "u", 0, "encoded", null, null, 1L);
		when(credentialGateway.findForLogin(CredentialDimension.USERNAME, "u")).thenReturn(Optional.of(account));

		UsernamePasswordLoginStrategy strategy = new UsernamePasswordLoginStrategy(credentialGateway,
				authProfileQueryPort, passwordEncoder, loginAuditService);

		UsernamePasswordCommand command = new UsernamePasswordCommand();
		command.setUsername("u");
		command.setPassword("p");

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(command, httpServletRequest));

		assertEquals(AuthResultCode.USER_DISABLED, exception.getResultCode());
		verify(loginAuditService).auditAccountDisabled(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_PASSWORD),
				eq(1L), eq("u"), eq(AuthResultCode.USER_DISABLED.getMessageKey()));
		verifyNoInteractions(passwordEncoder);
	}

	@Test
	@DisplayName("supports 返回 UsernamePasswordCommand.class")
	void supports_shouldReturnUsernamePasswordCommandClass() {
		UsernamePasswordLoginStrategy strategy = new UsernamePasswordLoginStrategy(credentialGateway,
				authProfileQueryPort, passwordEncoder, loginAuditService);

		assertEquals(UsernamePasswordCommand.class, strategy.supports());
	}

	@Test
	@DisplayName("密码错误时抛出 USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING")
	void authenticate_shouldThrowRemaining_whenPasswordMismatch() {
		LoginAccount account = new LoginAccount(1L, "u", 1, "encoded", null, null, 1L);
		when(credentialGateway.findForLogin(CredentialDimension.USERNAME, "u")).thenReturn(Optional.of(account));

		when(passwordEncoder.matches("p", "encoded")).thenReturn(false);
		doNothing().when(credentialGateway).assertNotLocked(1L);
		when(credentialGateway.recordFailure(1L)).thenReturn(2);

		UsernamePasswordLoginStrategy strategy = new UsernamePasswordLoginStrategy(credentialGateway,
				authProfileQueryPort, passwordEncoder, loginAuditService);

		UsernamePasswordCommand command = new UsernamePasswordCommand();
		command.setUsername("u");
		command.setPassword("p");

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(command, httpServletRequest));

		assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING, exception.getResultCode());
		verify(loginAuditService).auditBadCredentialsWithRemaining(same(httpServletRequest),
				eq(AuthLoginLogType.LOGIN_PASSWORD), eq(1L), eq("u"), eq(2));
	}

}
