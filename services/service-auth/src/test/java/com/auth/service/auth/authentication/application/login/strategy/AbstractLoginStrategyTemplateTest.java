package com.auth.service.auth.authentication.application.login.strategy;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.authentication.application.gateway.CredentialGateway;
import com.auth.service.auth.authentication.application.input.credential.AbstractCredentialLogin;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditService;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.model.LoginAccount;
import com.auth.service.auth.authentication.domain.model.LoginResult;
import com.auth.service.auth.authorization.domain.port.AuthProfileQueryPort;
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

import static org.junit.jupiter.api.Assertions.*;
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
	private CredentialGateway credentialGateway;

	@Mock
	private AuthProfileQueryPort authProfileQueryPort;

	@Mock
	private LoginAuditService loginAuditService;

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
		when(authProfileQueryPort.buildByUserId(TestConstants.USER_ID)).thenReturn(profile);

		TestStrategy strategy = new TestStrategy(credentialGateway, authProfileQueryPort, loginAuditService, account,
				null);

		LoginResult result = strategy.authenticate(new TestCredentialLogin(), httpServletRequest);

		verify(credentialGateway).assertNotLocked(TestConstants.USER_ID);
		verify(authProfileQueryPort).buildByUserId(TestConstants.USER_ID);
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

		when(credentialGateway.recordFailure(TestConstants.USER_ID)).thenReturn(3);

		TestStrategy strategy = new TestStrategy(credentialGateway, authProfileQueryPort, loginAuditService, account,
				new AuthBadCredentialsException("bad"));
		TestCredentialLogin credential = new TestCredentialLogin();

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(credential, httpServletRequest));

		assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING, exception.getResultCode());
		verify(credentialGateway).recordFailure(TestConstants.USER_ID);
		verify(loginAuditService).auditBadCredentialsWithRemaining(same(httpServletRequest),
				eq(AuthLoginLogType.LOGIN_PASSWORD), eq(TestConstants.USER_ID), eq("u"), eq(3));
	}

	static class TestCredentialLogin extends AbstractCredentialLogin {

		@Override
		public AuthLoginLogType loginLogType() {
			return AuthLoginLogType.LOGIN_PASSWORD;
		}

		@Override
		public String principalForAudit() {
			return "test";
		}

	}

	static class TestStrategy extends AbstractLoginStrategyTemplate<TestCredentialLogin> {

		private final LoginAccount accountToReturn;

		private final RuntimeException authenticateException;

		TestStrategy(CredentialGateway credentialGateway, AuthProfileQueryPort authProfileQueryPort,
				LoginAuditService loginAuditService, LoginAccount accountToReturn,
				RuntimeException authenticateException) {
			super(credentialGateway, authProfileQueryPort, loginAuditService);
			this.accountToReturn = accountToReturn;
			this.authenticateException = authenticateException;
		}

		@Override
		protected AuthLoginLogType loginLogType() {
			return AuthLoginLogType.LOGIN_PASSWORD;
		}

		@Override
		protected LoginAccount resolveAccount(TestCredentialLogin credential) {
			return accountToReturn;
		}

		@Override
		protected AuthLoginLogType doAuthenticate(TestCredentialLogin credential, LoginAccount account) {
			if (authenticateException != null) {
				throw authenticateException;
			}
			return AuthLoginLogType.LOGIN_PASSWORD;
		}

		@Override
		public Class<TestCredentialLogin> supports() {
			return TestCredentialLogin.class;
		}

	}

}
