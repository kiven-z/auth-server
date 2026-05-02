package com.auth.service.auth.infrastructure.strategy;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.starter.model.UserEntity;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.exception.BadCredentialsException;
import com.auth.service.auth.infrastructure.login.LoginFailureRateLimiter;
import com.auth.service.auth.infrastructure.login.LoginResult;
import com.auth.service.auth.infrastructure.profile.AuthProfileService;
import com.auth.service.auth.model.enums.AuthLoginLogResult;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.request.AbstractAuthentication;
import com.auth.service.auth.service.AuthLoginAuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AbstractAuthenticationStrategy 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class AbstractAuthenticationStrategyTest {

	@Mock
	private LoginFailureRateLimiter loginFailureRateLimiter;

	@Mock
	private AuthProfileService authProfileService;

	@Mock
	private AuthLoginAuditService loginLogService;

	@Mock
	private HttpServletRequest httpServletRequest;

	@Test
	@DisplayName("模板策略：成功路径会前置检查锁定并构建 profile + response")
	void authenticate_shouldBuildProfileAndResponse_whenSuccess() {
		// Arrange
		UserEntity user = new UserEntity();
		user.setId(TestConstants.USER_ID);
		user.setUsername("u");

		AuthProfile profile = AuthProfile.builder()
			.userId(TestConstants.USER_ID)
			.roles(List.of("R1"))
			.permissions(List.of("P1"))
			.permVersion(1L)
			.build();
		when(authProfileService.buildByUser(user)).thenReturn(profile);

		TestStrategy strategy = new TestStrategy(loginFailureRateLimiter, authProfileService, loginLogService, user,
				null);

		// Act
		LoginResult result = strategy.authenticate(new TestAuth(), httpServletRequest);

		// Assert
		verify(loginFailureRateLimiter).assertNotLocked(TestConstants.USER_ID);
		verify(authProfileService).buildByUser(user);
		assertNotNull(result);
		assertEquals(TestConstants.USER_ID, result.authProfile().getUserId());
		assertEquals(List.of("R1"), result.response().getRoles());
		assertEquals(List.of("P1"), result.response().getPermissions());
	}

	@Test
	@DisplayName("模板策略：凭证错误时记录失败次数、写审计并抛 USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING")
	void authenticate_shouldRecordFailureAndThrowBizException_whenBadCredentials() {
		// Arrange
		UserEntity user = new UserEntity();
		user.setId(TestConstants.USER_ID);
		user.setUsername("u");

		when(loginFailureRateLimiter.recordFailure(TestConstants.USER_ID)).thenReturn(3);

		TestStrategy strategy = new TestStrategy(loginFailureRateLimiter, authProfileService, loginLogService, user,
				new BadCredentialsException("bad"));

		TestAuth testAuth = new TestAuth();

		// Act
		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(testAuth, httpServletRequest));

		// Assert
		assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING, exception.getResultCode());
		verify(loginFailureRateLimiter).recordFailure(TestConstants.USER_ID);
		verify(loginLogService).recordLoginLog(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_PASSWORD),
				eq(AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR), eq(TestConstants.USER_ID), eq("u"),
				eq("remainingAttempts=3"), isNull());
	}

	static class TestAuth extends AbstractAuthentication {

	}

	static class TestStrategy extends AbstractAuthenticationStrategy<TestAuth> {

		private final UserEntity userToReturn;

		private final RuntimeException authenticateException;

		TestStrategy(LoginFailureRateLimiter loginFailureRateLimiter, AuthProfileService authProfileService,
				AuthLoginAuditService loginLogService, UserEntity userToReturn,
				RuntimeException authenticateException) {
			super(loginFailureRateLimiter, authProfileService, loginLogService);
			this.userToReturn = userToReturn;
			this.authenticateException = authenticateException;
		}

		@Override
		protected AuthLoginLogType loginLogType() {
			return AuthLoginLogType.LOGIN_PASSWORD;
		}

		@Override
		protected UserEntity resolveUser(TestAuth auth) {
			return userToReturn;
		}

		@Override
		protected AuthLoginLogType doAuthenticate(TestAuth auth, UserEntity user) {
			if (authenticateException != null) {
				throw authenticateException;
			}
			return AuthLoginLogType.LOGIN_PASSWORD;
		}

		@Override
		public Class<TestAuth> supports() {
			return TestAuth.class;
		}

	}

}
