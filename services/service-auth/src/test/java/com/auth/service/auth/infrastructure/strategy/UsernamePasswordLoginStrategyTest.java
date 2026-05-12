package com.auth.service.auth.infrastructure.strategy;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.infrastructure.login.LoginFailureRateLimiter;
import com.auth.service.auth.infrastructure.login.strategy.UsernamePasswordLoginStrategy;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.model.enums.AuthLoginLogResult;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.enums.AuthResultCode;
import com.auth.service.auth.model.request.login.UsernamePasswordLoginRequest;
import com.auth.service.auth.service.AuthLoginAuditService;
import com.auth.service.auth.service.AuthProfileQueryService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link UsernamePasswordLoginStrategy} ????
 *
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
	private AuthLoginAuditService loginLogService;

	@Mock
	private HttpServletRequest httpServletRequest;

	@Test
	@DisplayName("????????????? USERNAME_OR_PASSWORD_ERROR?????????????")
	void resolveUser_shouldThrow_whenUserNotFound() {
		// Arrange
		when(userMapper.selectUserByUsername("u")).thenReturn(null);
		UsernamePasswordLoginStrategy strategy = new UsernamePasswordLoginStrategy(userMapper, loginFailureRateLimiter,
				authProfileQueryService, passwordEncoder, loginLogService);

		UsernamePasswordLoginRequest auth = new UsernamePasswordLoginRequest();
		auth.setUsername("u");
		auth.setPassword("p");

		// Act
		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(auth, httpServletRequest));

		// Assert
		assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR, exception.getResultCode());
		verify(loginLogService).recordLoginLog(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_PASSWORD),
				eq(AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR), isNull(), eq("u"), eq(exception.getMessageKey()),
				isNull());
	}

	@Test
	@DisplayName("supports??? UsernamePasswordLoginRequest.class")
	void supports_shouldReturnUsernamePasswordLoginRequestClass() {
		// Arrange
		UsernamePasswordLoginStrategy strategy = new UsernamePasswordLoginStrategy(userMapper, loginFailureRateLimiter,
				authProfileQueryService, passwordEncoder, loginLogService);

		// Act & Assert
		assertEquals(UsernamePasswordLoginRequest.class, strategy.supports());
	}

	@Test
	@DisplayName("????????????? USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING")
	void authenticate_shouldThrowRemaining_whenPasswordMismatch() {
		// Arrange
		UserEntity user = new UserEntity();
		user.setId(1L);
		user.setUsername("u");
		user.setPassword("encoded");
		user.setStatus(1);
		when(userMapper.selectUserByUsername("u")).thenReturn(user);

		when(passwordEncoder.matches("p", "encoded")).thenReturn(false);
		doNothing().when(loginFailureRateLimiter).assertNotLocked(1L);
		when(loginFailureRateLimiter.recordFailure(1L)).thenReturn(2);

		UsernamePasswordLoginStrategy strategy = new UsernamePasswordLoginStrategy(userMapper, loginFailureRateLimiter,
				authProfileQueryService, passwordEncoder, loginLogService);

		UsernamePasswordLoginRequest auth = new UsernamePasswordLoginRequest();
		auth.setUsername("u");
		auth.setPassword("p");

		// Act
		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(auth, httpServletRequest));

		// Assert
		assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING, exception.getResultCode());
		verify(loginLogService).recordLoginLog(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_PASSWORD),
				eq(AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR), eq(1L), eq("u"), eq("remainingAttempts=2"),
				isNull());
	}

}
