package com.auth.module.security.starter.security.authenticate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.auth.module.security.autoconfig.pipeline.authenticate.AuthProfileSecurityContextPopulator;
import com.auth.module.security.autoconfig.pipeline.authenticate.InternalRequestAuthenticator;
import com.auth.module.security.autoconfig.pipeline.authenticate.RequestAuthenticator;
import com.auth.module.security.autoconfig.pipeline.authenticate.SecurityAuthExecutor;
import com.auth.module.security.autoconfig.service.AuthProfileCacheService;
import com.auth.module.security.contract.api.SecurityTokenKind;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.constants.SecurityInternalTokenConstants;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.auth.module.security.core.token.facade.SecurityTokenHandlerFacade;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import com.auth.common.jwt.model.JwtUserToken;

import jakarta.servlet.http.HttpServletRequest;

class SecurityAuthExecutorTest {

	private InternalRequestAuthenticator internalRequestAuthenticator;

	private RequestAuthenticator matchingAuthenticator;

	private RequestAuthenticator nonMatchingAuthenticator;

	private AuthProfileSecurityContextPopulator populator;

	private SecurityAuthExecutor executor;

	private HttpServletRequest request;

	@BeforeEach
	void setUp() {
		SecurityTokenHandlerFacade facade = mock(SecurityTokenHandlerFacade.class);
		AuthProfileCacheService cache = mock(AuthProfileCacheService.class);
		internalRequestAuthenticator = new InternalRequestAuthenticator(facade, cache);

		matchingAuthenticator = mock(RequestAuthenticator.class);
		nonMatchingAuthenticator = mock(RequestAuthenticator.class);
		populator = mock(AuthProfileSecurityContextPopulator.class);
		executor = new SecurityAuthExecutor(internalRequestAuthenticator,
				List.of(nonMatchingAuthenticator, matchingAuthenticator), populator);
		request = mock(HttpServletRequest.class);

		when(nonMatchingAuthenticator.supports(request)).thenReturn(false);
		when(matchingAuthenticator.supports(request)).thenReturn(true);
	}

	@Test
	@DisplayName("测试匹配认证器时应填充上下文")
	void require_withMatchingAuthenticator_populatesContext() {
		AuthProfile profile = AuthProfile.builder().userId(100L).username("alice").build();
		when(matchingAuthenticator.authenticate(request)).thenReturn(profile);

		executor.require(request);

		verify(populator).populate(profile);
		verify(matchingAuthenticator).authenticate(request);
		verify(nonMatchingAuthenticator, never()).authenticate(request);
	}

	@Test
	@DisplayName("测试不匹配认证器时应抛出异常")
	void require_noMatchingAuthenticator_throws() {
		when(matchingAuthenticator.supports(request)).thenReturn(false);

		SecurityTokenException ex = assertThrows(SecurityTokenException.class, () -> executor.require(request));
		assertEquals(SecurityResultCodeEnum.TOKEN_MISSING, ex.getResultCode());
		verify(populator, never()).populate(any());
	}

	@Test
	@DisplayName("requireInternal: 缺少内部令牌时应抛出缺令牌异常（即使存在外部匹配认证器也不允许）")
	void requireInternal_missingInternalToken_throwsEvenIfExternalAuthenticatorMatches() {
		when(matchingAuthenticator.supports(request)).thenReturn(true);
		when(request.getHeader(SecurityInternalTokenConstants.INTERNAL_HEADER)).thenReturn(null);

		SecurityTokenException ex = assertThrows(SecurityTokenException.class, () -> executor.requireInternal(request));
		assertEquals(SecurityResultCodeEnum.TOKEN_MISSING, ex.getResultCode());
		verify(populator, never()).populate(any());
	}

	@Test
	@DisplayName("requireInternal: 存在内部令牌时应进行内部认证并填充上下文")
	void requireInternal_withInternalToken_populatesContext() {
		SecurityTokenHandlerFacade facade = mock(SecurityTokenHandlerFacade.class);
		AuthProfileCacheService cache = mock(AuthProfileCacheService.class);
		internalRequestAuthenticator = new InternalRequestAuthenticator(facade, cache);
		executor = new SecurityAuthExecutor(internalRequestAuthenticator,
				List.of(nonMatchingAuthenticator, matchingAuthenticator), populator);

		when(request.getHeader(SecurityInternalTokenConstants.INTERNAL_HEADER)).thenReturn("svc.jwt");
		SecurityTokenResult result = SecurityTokenResult.builder()
			.kind(SecurityTokenKind.INTERNAL)
			.rawToken("svc.jwt")
			.userToken(JwtUserToken.builder().sub("0").build())
			.principalType(SecurityInternalTokenConstants.PRINCIPAL_TYPE_SERVICE)
			.serviceId("auth-server")
			.build();
		when(facade.validateInternal("svc.jwt")).thenReturn(result);

		executor.requireInternal(request);

		verify(populator, times(1)).populate(any(AuthProfile.class));
	}

	@Test
	@DisplayName("测试匹配认证器时应填充上下文")
	void tryAuthenticate_withMatchingAuthenticator_populatesContext() {
		AuthProfile profile = AuthProfile.builder().userId(100L).username("alice").build();
		when(matchingAuthenticator.authenticate(request)).thenReturn(profile);

		executor.tryAuthenticate(request);

		verify(populator).populate(profile);
	}

	@Test
	@DisplayName("测试不匹配认证器时应静默跳过")
	void tryAuthenticate_noMatchingAuthenticator_skipsSilently() {
		when(matchingAuthenticator.supports(request)).thenReturn(false);

		assertDoesNotThrow(() -> executor.tryAuthenticate(request));
		verify(populator, never()).populate(any());
	}

}
