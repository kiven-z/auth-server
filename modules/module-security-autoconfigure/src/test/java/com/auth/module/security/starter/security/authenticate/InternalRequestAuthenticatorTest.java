package com.auth.module.security.starter.security.authenticate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import com.auth.module.security.autoconfig.pipeline.authenticate.InternalRequestAuthenticator;
import com.auth.module.security.contract.constants.SecurityInternalTokenConstants;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import com.auth.module.security.core.token.provider.InternalTokenProvider;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class InternalRequestAuthenticatorTest {

	@Mock
	private InternalTokenProvider internalTokenProvider;

	@Test
	@DisplayName("测试支持判断：存在 X-Internal-JWT 时返回 true")
	void supports_shouldReturnTrue_whenInternalHeaderPresent() {
		InternalRequestAuthenticator authenticator = new InternalRequestAuthenticator(internalTokenProvider);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(SecurityInternalTokenConstants.INTERNAL_HEADER, "internal-token");

		assertTrue(authenticator.supports(request));
	}

	@Test
	@DisplayName("测试内部服务令牌认证成功并写入服务角色")
	void authenticate_shouldReturnServiceProfile_whenPrincipalTypeIsService() {
		InternalRequestAuthenticator authenticator = new InternalRequestAuthenticator(internalTokenProvider);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(SecurityInternalTokenConstants.INTERNAL_HEADER, "internal-token");
		when(internalTokenProvider.parseToken("internal-token")).thenReturn(SecurityTokenResult.builder()
			.principalType(SecurityInternalTokenConstants.PRINCIPAL_TYPE_SERVICE)
			.serviceId("service-auth")
			.build());

		var profile = authenticator.authenticate(request);

		assertEquals("service-auth", profile.getUsername());
		assertNotNull(profile.getRoles());
		assertTrue(profile.getRoles().contains(SecurityInternalTokenConstants.ROLE_INTERNAL_SERVICE));
		verify(internalTokenProvider).parseToken("internal-token");
	}

	@Test
	@DisplayName("测试内部接口拒绝用户主体令牌")
	void authenticate_shouldThrowTokenInvalid_whenPrincipalTypeIsUser() {
		InternalRequestAuthenticator authenticator = new InternalRequestAuthenticator(internalTokenProvider);
		HttpServletRequest request = new MockHttpServletRequest();
		((MockHttpServletRequest) request).addHeader(SecurityInternalTokenConstants.INTERNAL_HEADER, "user-token");
		when(internalTokenProvider.parseToken("user-token")).thenReturn(SecurityTokenResult.builder()
			.principalType(SecurityInternalTokenConstants.PRINCIPAL_TYPE_USER)
			.build());

		SecurityTokenException ex = assertThrows(SecurityTokenException.class, () -> authenticator.authenticate(request));
		assertEquals(SecurityResultCodeEnum.TOKEN_INVALID, ex.getResultCode());
	}

}
