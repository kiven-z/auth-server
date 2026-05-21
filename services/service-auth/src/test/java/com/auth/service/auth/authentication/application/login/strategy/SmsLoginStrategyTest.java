package com.auth.service.auth.authentication.application.login.strategy;

import cn.hutool.crypto.digest.MD5;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.authentication.application.command.SmsLoginCommand;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditService;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.enums.CredentialDimension;
import com.auth.service.auth.authentication.domain.model.LoginAccount;
import com.auth.service.auth.authentication.domain.port.LoginAccountLookupPort;
import com.auth.service.auth.authentication.domain.port.LoginFailureRateLimitPort;
import com.auth.service.auth.authentication.domain.port.LoginVerificationCodePort;
import com.auth.service.auth.authorization.domain.port.AuthProfileQueryPort;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

/**
 * {@link SmsLoginStrategy} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class SmsLoginStrategyTest {

	@Mock
	private LoginAccountLookupPort loginAccountLookupPort;

	@Mock
	private LoginVerificationCodePort loginVerificationCodePort;

	@Mock
	private LoginFailureRateLimitPort loginFailureRateLimitPort;

	@Mock
	private AuthProfileQueryPort authProfileQueryPort;

	@Mock
	private LoginAuditService loginAuditService;

	@Mock
	private HttpServletRequest httpServletRequest;

	private SmsLoginStrategy createStrategy() {
		return new SmsLoginStrategy(loginAccountLookupPort, loginFailureRateLimitPort, authProfileQueryPort,
				loginVerificationCodePort, loginAuditService);
	}

	@Test
	@DisplayName("手机号未注册时抛出 USERNAME_OR_PASSWORD_ERROR 并发布审计事件")
	void authenticate_shouldThrow_whenPhoneNotRegistered() {
		when(loginAccountLookupPort.findForLogin(CredentialDimension.PHONE, "13800138000"))
			.thenReturn(Optional.empty());
		SmsLoginStrategy strategy = createStrategy();

		SmsLoginCommand command = new SmsLoginCommand("13800138000", "123456");
		when(loginAuditService.resolvePrincipal(command)).thenReturn("13800138000");

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(command, httpServletRequest));

		assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR, exception.getResultCode());
		verify(loginAuditService).resolvePrincipal(command);
		verify(loginAuditService).auditUserUnknownAfterResolve(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_SMS),
				eq("13800138000"), eq(exception.getMessageKey()));
	}

	@Test
	@DisplayName("验证码错误时抛出 AUTH_CODE_ERROR")
	void doAuthenticate_shouldThrow_whenCodeMismatch() {
		LoginAccount account = new LoginAccount(1L, "testUser", 1, null, null, null, 1L);
		when(loginAccountLookupPort.findForLogin(CredentialDimension.PHONE, "13800138000"))
			.thenReturn(Optional.of(account));
		doNothing().when(loginFailureRateLimitPort).assertNotLocked(1L);

		when(loginVerificationCodePort.getStoredDigest(CredentialDimension.PHONE, "13800138000"))
			.thenReturn(MD5.create().digestHex16("wrong"));

		SmsLoginStrategy strategy = createStrategy();

		SmsLoginCommand command = new SmsLoginCommand("13800138000", "000000");

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(command, httpServletRequest));

		assertEquals(AuthResultCode.AUTH_CODE_ERROR, exception.getResultCode());
		verify(loginAuditService).auditMappedCredentialFailure(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_SMS),
				eq(1L), eq("testUser"), eq(exception));
	}

	@Test
	@DisplayName("验证码正确时登录成功并删除 Redis 中的验证码")
	void doAuthenticate_shouldSucceed_whenCodeMatches() {
		LoginAccount account = new LoginAccount(1L, "testUser", 1, null, "Test", null, 1L);
		when(loginAccountLookupPort.findForLogin(CredentialDimension.PHONE, "13800138000"))
			.thenReturn(Optional.of(account));
		doNothing().when(loginFailureRateLimitPort).assertNotLocked(1L);

		AuthProfile profile = mock(AuthProfile.class);
		when(authProfileQueryPort.buildByUserId(1L)).thenReturn(profile);

		String phone = "13800138000";
		String code = "123456";
		when(loginVerificationCodePort.getStoredDigest(CredentialDimension.PHONE, phone))
			.thenReturn(MD5.create().digestHex16(code));

		SmsLoginStrategy strategy = createStrategy();
		SmsLoginCommand command = new SmsLoginCommand(phone, code);

		strategy.authenticate(command, httpServletRequest);

		verify(loginVerificationCodePort).deleteCode(CredentialDimension.PHONE, phone);
	}

	@Test
	@DisplayName("supports 返回 SmsLoginCommand.class")
	void supports_shouldReturnSmsLoginCommandClass() {
		SmsLoginStrategy strategy = createStrategy();
		assertEquals(SmsLoginCommand.class, strategy.supports());
	}

}
