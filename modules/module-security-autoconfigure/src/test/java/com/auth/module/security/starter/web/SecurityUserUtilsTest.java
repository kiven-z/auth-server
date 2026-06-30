package com.auth.module.security.starter.web;

import com.auth.module.security.autoconfigure.security.SecurityRequestAttributes;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link SecurityUserUtils} 单元测试
 */
@DisplayName("SecurityUserUtils 安全上下文工具")
class SecurityUserUtilsTest {

	@AfterEach
	void tearDown() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	@DisplayName("getSessionId：请求属性含 sessionId 时返回")
	void getSessionId_whenRequestAttributePresent_returnsSessionId() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setAttribute(SecurityRequestAttributes.SESSION_ID, "jti-abc");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		assertEquals("jti-abc", SecurityUserUtils.getSessionId());
	}

	@Test
	@DisplayName("getSessionId：无请求上下文时返回 null")
	void getSessionId_whenNoRequestContext_returnsNull() {
		assertNull(SecurityUserUtils.getSessionId());
	}

	@Test
	@DisplayName("getSessionId：请求属性非 String 时返回 null")
	void getSessionId_whenAttributeNotString_returnsNull() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setAttribute(SecurityRequestAttributes.SESSION_ID, 123);
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		assertNull(SecurityUserUtils.getSessionId());
	}

}
