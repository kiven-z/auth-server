package com.auth.service.auth.support.message;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AuthMessageVariables} 单元测试
 */
@DisplayName("AuthMessageVariables 认证邮件变量")
class AuthMessageVariablesTest {

	@Test
	@DisplayName("loginTime 应按 UTC 钟点格式化并带 UTC 后缀")
	void buildLoginEmail_shouldFormatLoginTimeWithUtcSuffix() {
		Instant loginTime = LocalDateTime.of(2026, 7, 29, 10, 6, 0).toInstant(ZoneOffset.UTC);

		Map<String, Object> variables = AuthMessageVariables.buildLoginEmail("admin", "123456", 10L, loginTime,
				"127.0.0.1", "局域网");

		assertEquals("2026-07-29 10:06:00 UTC", variables.get("loginTime"));
		assertEquals("admin", variables.get("username"));
		assertEquals("123456", variables.get("verificationCode"));
		assertEquals(10L, variables.get("expireTime"));
	}

	@Test
	@DisplayName("loginTime 为空时应写入空串而非抛异常")
	void buildLoginEmail_shouldUseEmptyStringWhenLoginTimeNull() {
		Map<String, Object> variables = AuthMessageVariables.buildLoginEmail("admin", "123456", 5L, null, "1.1.1.1",
				null);

		assertEquals("", variables.get("loginTime"));
		assertTrue(variables.containsKey("loginTime"));
	}

}
