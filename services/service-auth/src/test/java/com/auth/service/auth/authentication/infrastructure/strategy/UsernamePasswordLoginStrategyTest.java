package com.auth.service.auth.authentication.infrastructure.strategy;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.authentication.application.command.UsernamePasswordLoginCommand;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogResult;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.event.LoginAuditEventPublisher;
import com.auth.service.auth.authentication.infrastructure.persistence.UserMapper;
import com.auth.service.auth.authentication.infrastructure.token.LoginFailureRateLimiter;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link UsernamePasswordLoginStrategy}
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class UsernamePasswordLoginStrategyTest {

	@Mock
	private UserMapper userMapper;

	@Mock
	private LoginFailureRateLimiter loginFailureRateLimiter;

	@Mock
	private AuthProfileQueryService authProfileQueryService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private LoginAuditEventPublisher loginAuditEventPublisher;

	@Mock
	private HttpServletRequest httpServletRequest;

	@Test
	@DisplayName("用户不存在时抛出 USERNAME_OR_PASSWORD_ERROR 并记录审计")
	void resolveUser_shouldThrow_whenUserNotFound() {
		when(userMapper.selectUserForLoginByUsername("u")).thenReturn(null);
		UsernamePasswordLoginStrategy strategy = new UsernamePasswordLoginStrategy(userMapper, loginFailureRateLimiter,
				authProfileQueryService, passwordEncoder, loginAuditEventPublisher);

		UsernamePasswordLoginCommand command = new UsernamePasswordLoginCommand("u", "p");

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(command, httpServletRequest));

		assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR, exception.getResultCode());
		verify(loginAuditEventPublisher).publish(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_PASSWORD),
				eq(AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR), isNull(), eq("u"), eq(exception.getMessageKey()),
				isNull());
	}

	@Test
	@DisplayName("账号禁用时抛出 USER_DISABLED 并记录审计")
	void authenticate_shouldThrowUserDisabled_whenAccountDisabled() {
		UserEntity user = new UserEntity();
		user.setId(1L);
		user.setUsername("u");
		user.setPassword("encoded");
		user.setStatus(0);
		when(userMapper.selectUserForLoginByUsername("u")).thenReturn(user);

		UsernamePasswordLoginStrategy strategy = new UsernamePasswordLoginStrategy(userMapper, loginFailureRateLimiter,
				authProfileQueryService, passwordEncoder, loginAuditEventPublisher);

		UsernamePasswordLoginCommand command = new UsernamePasswordLoginCommand("u", "p");

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(command, httpServletRequest));

		assertEquals(AuthResultCode.USER_DISABLED, exception.getResultCode());
		verify(loginAuditEventPublisher).publish(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_PASSWORD),
				eq(AuthLoginLogResult.ACCOUNT_DISABLED), eq(1L), eq("u"),
				eq(AuthResultCode.USER_DISABLED.getMessageKey()), isNull());
		verifyNoInteractions(passwordEncoder);
	}

	@Test
	@DisplayName("supports 返回 UsernamePasswordLoginCommand.class")
	void supports_shouldReturnUsernamePasswordLoginCommandClass() {
		UsernamePasswordLoginStrategy strategy = new UsernamePasswordLoginStrategy(userMapper, loginFailureRateLimiter,
				authProfileQueryService, passwordEncoder, loginAuditEventPublisher);

		assertEquals(UsernamePasswordLoginCommand.class, strategy.supports());
	}

	@Test
	@DisplayName("密码错误时抛出 USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING")
	void authenticate_shouldThrowRemaining_whenPasswordMismatch() {
		UserEntity user = new UserEntity();
		user.setId(1L);
		user.setUsername("u");
		user.setPassword("encoded");
		user.setStatus(1);
		when(userMapper.selectUserForLoginByUsername("u")).thenReturn(user);

		when(passwordEncoder.matches("p", "encoded")).thenReturn(false);
		doNothing().when(loginFailureRateLimiter).assertNotLocked(1L);
		when(loginFailureRateLimiter.recordFailure(1L)).thenReturn(2);

		UsernamePasswordLoginStrategy strategy = new UsernamePasswordLoginStrategy(userMapper, loginFailureRateLimiter,
				authProfileQueryService, passwordEncoder, loginAuditEventPublisher);

		UsernamePasswordLoginCommand command = new UsernamePasswordLoginCommand("u", "p");

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(command, httpServletRequest));

		assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING, exception.getResultCode());
		verify(loginAuditEventPublisher).publish(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_PASSWORD),
				eq(AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR), eq(1L), eq("u"), eq("remainingAttempts=2"),
				isNull());
	}

}
