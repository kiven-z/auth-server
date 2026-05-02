package com.auth.module.security.starter.feign;

import com.auth.module.security.autoconfig.feign.InternalJwtFeignRequestInterceptor;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.constants.SecurityInternalTokenConstants;
import com.auth.module.security.core.token.provider.InternalTokenProvider;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class InternalJwtFeignRequestInterceptorTest {

	private static final String SERVICE_ID = "auth-server";

	@AfterEach
	void cleanup() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("测试有认证配置时应签名用户令牌并移除授权")
	void apply_withAuthProfile_shouldSignUserTokenAndStripAuthorization() {
		InternalTokenProvider provider = mock(InternalTokenProvider.class);
		when(provider.buildToken(eq(123L), anyString())).thenReturn("user.internal.jwt");

		InternalJwtFeignRequestInterceptor interceptor = new InternalJwtFeignRequestInterceptor(provider, SERVICE_ID);

		AuthProfile profile = AuthProfile.builder().userId(123L).username("u").build();
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(profile, null));

		RequestTemplate template = new RequestTemplate();
		template.header(HttpHeaders.AUTHORIZATION, "Bearer external.token");

		interceptor.apply(template);

		Collection<String> authValues = template.headers().get(HttpHeaders.AUTHORIZATION);
		assertTrue(authValues == null || authValues.isEmpty(), "Authorization should be stripped on outbound");
		Collection<String> internal = template.headers().get(SecurityInternalTokenConstants.INTERNAL_HEADER);
		assertEquals(1, internal.size());
		assertEquals("user.internal.jwt", internal.iterator().next());
		verify(provider).buildToken(eq(123L), anyString());
		verify(provider, never()).buildServiceToken(anyString(), anyString());
	}

	@Test
	@DisplayName("测试没有认证配置时应签名服务令牌并移除授权")
	void apply_withoutAuthProfile_shouldSignServiceTokenAndStripAuthorization() {
		InternalTokenProvider provider = mock(InternalTokenProvider.class);
		when(provider.buildServiceToken(eq(SERVICE_ID), anyString())).thenReturn("svc.internal.jwt");

		InternalJwtFeignRequestInterceptor interceptor = new InternalJwtFeignRequestInterceptor(provider, SERVICE_ID);
		SecurityContextHolder.clearContext();

		RequestTemplate template = new RequestTemplate();
		template.header(HttpHeaders.AUTHORIZATION, "Bearer external.token");

		interceptor.apply(template);

		Collection<String> authValues = template.headers().get(HttpHeaders.AUTHORIZATION);
		assertTrue(authValues == null || authValues.isEmpty(), "Authorization should be stripped on outbound");
		Collection<String> internal = template.headers().get(SecurityInternalTokenConstants.INTERNAL_HEADER);
		assertEquals(1, internal.size());
		assertEquals("svc.internal.jwt", internal.iterator().next());
		verify(provider).buildServiceToken(eq(SERVICE_ID), anyString());
		verify(provider, never()).buildToken(anyLong(), anyString());
	}

	@Test
	@DisplayName("测试有配置但无用户ID时应回退到服务令牌")
	void apply_withProfileButNoUserId_shouldFallbackToServiceToken() {
		InternalTokenProvider provider = mock(InternalTokenProvider.class);
		when(provider.buildServiceToken(eq(SERVICE_ID), anyString())).thenReturn("svc.internal.jwt");

		InternalJwtFeignRequestInterceptor interceptor = new InternalJwtFeignRequestInterceptor(provider, SERVICE_ID);

		AuthProfile profile = AuthProfile.builder().username("anonymous").build();
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(profile, null));

		RequestTemplate template = new RequestTemplate();
		interceptor.apply(template);

		Collection<String> internal = template.headers().get(SecurityInternalTokenConstants.INTERNAL_HEADER);
		assertEquals(1, internal.size());
		assertEquals("svc.internal.jwt", internal.iterator().next());
		verify(provider).buildServiceToken(eq(SERVICE_ID), anyString());
		verify(provider, never()).buildToken(anyLong(), anyString());
	}

}
