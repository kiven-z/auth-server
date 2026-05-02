package com.auth.module.security.starter.security.authenticate;

import static com.auth.module.security.contract.constants.SecurityInternalTokenConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.autoconfig.pipeline.authenticate.InternalRequestAuthenticator;
import com.auth.module.security.autoconfig.service.AuthProfileCacheService;
import com.auth.module.security.contract.api.SecurityTokenKind;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.constants.SecurityInternalTokenConstants;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.auth.module.security.core.token.facade.SecurityTokenHandlerFacade;
import com.auth.module.security.core.token.model.SecurityTokenResult;

import jakarta.servlet.http.HttpServletRequest;

class InternalRequestAuthenticatorTest {

	private static HttpServletRequest mockRequest(String internalToken) {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getHeader(SecurityInternalTokenConstants.INTERNAL_HEADER)).thenReturn(internalToken);
		return request;
	}

	@Test
	@DisplayName("测试服务令牌应返回合成配置而不进行缓存查找")
	void authenticate_serviceToken_shouldReturnSyntheticProfileWithoutCacheLookup() {
		SecurityTokenHandlerFacade facade = mock(SecurityTokenHandlerFacade.class);
		AuthProfileCacheService cache = mock(AuthProfileCacheService.class);

		SecurityTokenResult result = SecurityTokenResult.builder()
			.kind(SecurityTokenKind.INTERNAL)
			.rawToken("svc.jwt")
			.userToken(JwtUserToken.builder().sub("0").build())
			.principalType(SecurityInternalTokenConstants.PRINCIPAL_TYPE_SERVICE)
			.serviceId("auth-server")
			.build();
		when(facade.validateInternal("svc.jwt")).thenReturn(result);

		InternalRequestAuthenticator authenticator = new InternalRequestAuthenticator(facade, cache);
		AuthProfile profile = authenticator.authenticate(mockRequest("svc.jwt"));

		assertNotNull(profile);
		assertEquals("auth-server", profile.getUsername());
		assertTrue(profile.getRoles().contains(SecurityInternalTokenConstants.ROLE_INTERNAL_SERVICE));
		verify(cache, never()).load(anyLong());
	}

	@Test
	@DisplayName("测试用户令牌应从缓存加载配置")
	void authenticate_userToken_shouldLoadProfileFromCache() {
		SecurityTokenHandlerFacade facade = mock(SecurityTokenHandlerFacade.class);
		AuthProfileCacheService cache = mock(AuthProfileCacheService.class);

		SecurityTokenResult result = SecurityTokenResult.builder()
			.kind(SecurityTokenKind.INTERNAL)
			.rawToken("user.jwt")
			.userToken(JwtUserToken.builder().userId(123L).sub("123").build())
			.principalType(PRINCIPAL_TYPE_USER)
			.build();
		when(facade.validateInternal("user.jwt")).thenReturn(result);

		AuthProfile cached = AuthProfile.builder().userId(123L).username("alice").build();
		when(cache.load(123L)).thenReturn(cached);

		InternalRequestAuthenticator authenticator = new InternalRequestAuthenticator(facade, cache);
		AuthProfile profile = authenticator.authenticate(mockRequest("user.jwt"));

		assertEquals(cached, profile);
		verify(cache).load(123L);
	}

	@Test
	@DisplayName("测试用户令牌缺失用户ID时应抛出令牌无效异常")
	void authenticate_userToken_missingUserId_shouldThrowTokenInvalid() {
		SecurityTokenHandlerFacade facade = mock(SecurityTokenHandlerFacade.class);
		AuthProfileCacheService cache = mock(AuthProfileCacheService.class);
		SecurityTokenResult result = SecurityTokenResult.builder()
			.kind(SecurityTokenKind.INTERNAL)
			.rawToken("user.jwt")
			.userToken(JwtUserToken.builder().build())
			.principalType(PRINCIPAL_TYPE_USER)
			.build();
		when(facade.validateInternal("user.jwt")).thenReturn(result);
		InternalRequestAuthenticator authenticator = new InternalRequestAuthenticator(facade, cache);
		HttpServletRequest request = mockRequest("user.jwt");
		SecurityTokenException ex = assertThrows(SecurityTokenException.class,
				() -> authenticator.authenticate(request));
		assertEquals(SecurityResultCodeEnum.TOKEN_INVALID, ex.getResultCode());
		verify(cache, never()).load(anyLong());
	}

	@Test
	@DisplayName("测试用户令牌缺失配置时应抛出会话缺失异常")
	void authenticate_userToken_missingProfile_shouldThrowSessionMissing() {
		SecurityTokenHandlerFacade facade = mock(SecurityTokenHandlerFacade.class);
		AuthProfileCacheService cache = mock(AuthProfileCacheService.class);

		SecurityTokenResult result = SecurityTokenResult.builder()
			.kind(SecurityTokenKind.INTERNAL)
			.rawToken("user.jwt")
			.userToken(JwtUserToken.builder().userId(404L).build())
			.principalType(PRINCIPAL_TYPE_USER)
			.build();
		when(facade.validateInternal("user.jwt")).thenReturn(result);
		when(cache.load(404L)).thenReturn(null);

		InternalRequestAuthenticator authenticator = new InternalRequestAuthenticator(facade, cache);
		HttpServletRequest request = mockRequest("user.jwt");
		SecurityTokenException ex = assertThrows(SecurityTokenException.class,
				() -> authenticator.authenticate(request));
		assertEquals(SecurityResultCodeEnum.SESSION_MISSING, ex.getResultCode());
	}

}
