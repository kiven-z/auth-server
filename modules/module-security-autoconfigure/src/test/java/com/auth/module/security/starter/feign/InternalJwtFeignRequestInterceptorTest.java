package com.auth.module.security.starter.feign;

import com.auth.module.security.autoconfigure.feign.InternalJwtFeignRequestInterceptor;
import com.auth.module.security.contract.constants.SecurityInternalTokenConstants;
import com.auth.module.security.core.token.provider.InternalTokenProvider;
import feign.RequestTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class InternalJwtFeignRequestInterceptorTest {

	private static final String SERVICE_ID = "auth-server";

	@Test
	@DisplayName("测试应签发服务令牌并移除外部授权头")
	void apply_shouldSignServiceTokenAndStripAuthorization() {
		InternalTokenProvider provider = mock(InternalTokenProvider.class);
		when(provider.buildServiceToken(eq(SERVICE_ID), anyString())).thenReturn("svc.internal.jwt");

		InternalJwtFeignRequestInterceptor interceptor = new InternalJwtFeignRequestInterceptor(provider, SERVICE_ID);

		RequestTemplate template = new RequestTemplate();
		template.header(HttpHeaders.AUTHORIZATION, "Bearer external.token");

		interceptor.apply(template);

		Collection<String> authValues = template.headers().get(HttpHeaders.AUTHORIZATION);
		assertTrue(authValues == null || authValues.isEmpty(), "Authorization should be stripped on outbound");
		Collection<String> internal = template.headers().get(SecurityInternalTokenConstants.INTERNAL_HEADER);
		assertEquals(1, internal.size());
		assertEquals("svc.internal.jwt", internal.iterator().next());
		verify(provider).buildServiceToken(eq(SERVICE_ID), anyString());
	}

	@Test
	@DisplayName("测试应覆盖已有内部令牌头，避免多值污染")
	void apply_shouldReplaceExistingInternalHeader() {
		InternalTokenProvider provider = mock(InternalTokenProvider.class);
		when(provider.buildServiceToken(eq(SERVICE_ID), anyString())).thenReturn("svc.internal.jwt");

		InternalJwtFeignRequestInterceptor interceptor = new InternalJwtFeignRequestInterceptor(provider, SERVICE_ID);

		RequestTemplate template = new RequestTemplate();
		template.header(SecurityInternalTokenConstants.INTERNAL_HEADER, "stale-token");
		interceptor.apply(template);

		Collection<String> internal = template.headers().get(SecurityInternalTokenConstants.INTERNAL_HEADER);
		assertEquals(1, internal.size());
		assertEquals("svc.internal.jwt", internal.iterator().next());
		verify(provider).buildServiceToken(eq(SERVICE_ID), anyString());
	}

}
