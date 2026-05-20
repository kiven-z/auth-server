package com.auth.service.auth.authentication.application.login.strategy;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.authentication.application.command.AbstractLoginCommand;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditCoordinator;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.model.LoginAccount;
import com.auth.service.auth.authentication.domain.model.LoginResult;
import com.auth.service.auth.authentication.domain.port.LoginFailureRateLimitPort;
import com.auth.service.auth.authorization.application.AuthProfileQueryService;
import com.auth.service.auth.shared.exception.AuthBadCredentialsException;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AbstractLoginStrategyTemplate} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class AbstractLoginStrategyTemplateTest {

	@Mock
	private LoginFailureRateLimitPort loginFailureRateLimitPort;

	@Mock
	private AuthProfileQueryService authProfileQueryService;

	@Mock
	private LoginAuditCoordinator loginAuditCoordinator;

	@Mock
	private HttpServletRequest httpServletRequest;

	@Test
	@DisplayName("模板策略：成功路径会前置检查锁定并构建 profile + response")
	void authenticate_shouldBuildProfileAndResponse_whenSuccess() {
		LoginAccount account = new LoginAccount(TestConstants.USER_ID, "u", 1, "encoded", "nick", "avatar", 1L);

		AuthProfile profile = AuthProfile.builder()
			.userId(TestConstants.USER_ID)
			.roles(List.of("R1"))
			.permissions(List.of("P1"))
			.permVersion(1L)
			.build();
		when(authProfileQueryService.buildByUserId(TestConstants.USER_ID)).thenReturn(profile);

		TestStrategy strategy = new TestStrategy(loginFailureRateLimitPort, authProfileQueryService,
				loginAuditCoordinator, account, null);

		LoginResult result = strategy.authenticate(new TestLoginCommand(), httpServletRequest);

		verify(loginFailureRateLimitPort).assertNotLocked(TestConstants.USER_ID);
		verify(authProfileQueryService).buildByUserId(TestConstants.USER_ID);
		assertNotNull(result);
		assertEquals(TestConstants.USER_ID, result.authProfile().getUserId());
		assertEquals(TestConstants.USER_ID, result.authenticatedUser().id());
		assertEquals(List.of("R1"), result.authProfile().getRoles());
		assertEquals(List.of("P1"), result.authProfile().getPermissions());
	}

	@Test
	@DisplayName("模板策略：凭证错误时记录失败次数、写审计并抛 USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING")
	void authenticate_shouldRecordFailureAndThrowBizException_whenBadCredentials() {
		LoginAccount account = new LoginAccount(TestConstants.USER_ID, "u", 1, "encoded", null, null, 1L);

		when(loginFailureRateLimitPort.recordFailure(TestConstants.USER_ID)).thenReturn(3);

		TestStrategy strategy = new TestStrategy(loginFailureRateLimitPort, authProfileQueryService,
				loginAuditCoordinator, account, new AuthBadCredentialsException("bad"));
		TestLoginCommand command = new TestLoginCommand();

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(command, httpServletRequest));

		assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING, exception.getResultCode());
		verify(loginFailureRateLimitPort).recordFailure(TestConstants.USER_ID);
		verify(loginAuditCoordinator).auditBadCredentialsWithRemaining(same(httpServletRequest),
				eq(AuthLoginLogType.LOGIN_PASSWORD), eq(TestConstants.USER_ID), eq("u"), eq(3));
	}

	static class TestLoginCommand extends AbstractLoginCommand {

		@Override
		public AuthLoginLogType loginLogType() {
			return AuthLoginLogType.LOGIN_PASSWORD;
		}

		@Override
		public String principalForAudit() {
			return "test";
		}

	}

	static class TestStrategy extends AbstractLoginStrategyTemplate<TestLoginCommand> {

		private final LoginAccount accountToReturn;

		private final RuntimeException authenticateException;

		TestStrategy(LoginFailureRateLimitPort loginFailureRateLimitPort,
				AuthProfileQueryService authProfileQueryService, LoginAuditCoordinator loginAuditCoordinator,
				LoginAccount accountToReturn, RuntimeException authenticateException) {
			super(loginFailureRateLimitPort, authProfileQueryService, loginAuditCoordinator);
			this.accountToReturn = accountToReturn;
			this.authenticateException = authenticateException;
		}

		@Override
		protected AuthLoginLogType loginLogType() {
			return AuthLoginLogType.LOGIN_PASSWORD;
		}

		@Override
		protected LoginAccount resolveAccount(TestLoginCommand command) {
			return accountToReturn;
		}

		@Override
		protected AuthLoginLogType doAuthenticate(TestLoginCommand command, LoginAccount account) {
			if (authenticateException != null) {
				throw authenticateException;
			}
			return AuthLoginLogType.LOGIN_PASSWORD;
		}

		@Override
		public Class<TestLoginCommand> supports() {
			return TestLoginCommand.class;
		}

	}

}
