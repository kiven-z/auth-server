package com.auth.service.auth.application.login.strategy;

import cn.hutool.crypto.digest.MD5;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.application.login.audit.LoginAuditService;
import com.auth.service.auth.application.login.input.credential.SmsLogin;
import com.auth.service.auth.application.port.AuthProfileQueryPort;
import com.auth.service.auth.application.port.CredentialGateway;
import com.auth.service.auth.domain.authentication.enums.AuthLoginLogType;
import com.auth.service.auth.domain.authentication.enums.CredentialDimension;
import com.auth.service.auth.domain.authentication.model.LoginAccount;
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
	private CredentialGateway credentialGateway;

	@Mock
	private AuthProfileQueryPort authProfileQueryPort;

	@Mock
	private LoginAuditService loginAuditService;

	@Mock
	private HttpServletRequest httpServletRequest;

	private SmsLoginStrategy createStrategy() {
		return new SmsLoginStrategy(credentialGateway, authProfileQueryPort, loginAuditService);
	}

	@Test
	@DisplayName("手机号未注册时抛出 USERNAME_OR_PASSWORD_ERROR 并发布审计事件")
	void authenticate_shouldThrow_whenPhoneNotRegistered() {
		when(credentialGateway.findForLogin(CredentialDimension.PHONE, "13800138000")).thenReturn(Optional.empty());
		SmsLoginStrategy strategy = createStrategy();

		SmsLogin credential = new SmsLogin();
		credential.setPhone("13800138000");
		credential.setCode("123456");
		when(loginAuditService.resolvePrincipal(credential)).thenReturn("13800138000");

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(credential, httpServletRequest));

		assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR, exception.getResultCode());
		verify(loginAuditService).resolvePrincipal(credential);
		verify(loginAuditService).auditUserUnknownAfterResolve(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_SMS),
				eq("13800138000"), eq(exception.getMessageKey()));
	}

	@Test
	@DisplayName("验证码错误时抛出 AUTH_CODE_ERROR")
	void doAuthenticate_shouldThrow_whenCodeMismatch() {
		LoginAccount account = new LoginAccount(1L, "testUser", 1, null, null, null, 1L);
		when(credentialGateway.findForLogin(CredentialDimension.PHONE, "13800138000")).thenReturn(Optional.of(account));
		doNothing().when(credentialGateway).assertNotLocked(1L);

		when(credentialGateway.getStoredDigest(CredentialDimension.PHONE, "13800138000"))
			.thenReturn(MD5.create().digestHex16("wrong"));

		SmsLoginStrategy strategy = createStrategy();

		SmsLogin credential = new SmsLogin();
		credential.setPhone("13800138000");
		credential.setCode("000000");

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(credential, httpServletRequest));

		assertEquals(AuthResultCode.AUTH_CODE_ERROR, exception.getResultCode());
		verify(loginAuditService).auditMappedCredentialFailure(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_SMS),
				eq(1L), eq("testUser"), eq(exception));
	}

	@Test
	@DisplayName("验证码正确时登录成功并删除 Redis 中的验证码")
	void doAuthenticate_shouldSucceed_whenCodeMatches() {
		LoginAccount account = new LoginAccount(1L, "testUser", 1, null, "Test", null, 1L);
		when(credentialGateway.findForLogin(CredentialDimension.PHONE, "13800138000")).thenReturn(Optional.of(account));
		doNothing().when(credentialGateway).assertNotLocked(1L);

		AuthProfile profile = mock(AuthProfile.class);
		when(authProfileQueryPort.buildByUserId(1L)).thenReturn(profile);

		String phone = "13800138000";
		String code = "123456";
		when(credentialGateway.getStoredDigest(CredentialDimension.PHONE, phone))
			.thenReturn(MD5.create().digestHex16(code));

		SmsLoginStrategy strategy = createStrategy();
		SmsLogin credential = new SmsLogin();
		credential.setPhone(phone);
		credential.setCode(code);

		strategy.authenticate(credential, httpServletRequest);

		verify(credentialGateway).deleteCode(CredentialDimension.PHONE, phone);
	}

	@Test
	@DisplayName("supports 返回 SmsLogin.class")
	void supports_shouldReturnSmsLoginClass() {
		SmsLoginStrategy strategy = createStrategy();
		assertEquals(SmsLogin.class, strategy.supports());
	}

}
