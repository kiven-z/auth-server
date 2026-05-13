package com.auth.module.security.contract.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link OperationLogPayload} 单元测试
 *
 * @author Bunny
 */
class OperationLogPayloadTest {

	@Test
	@DisplayName("builder 应填充核心审计字段")
	void builder_shouldPopulateCoreFields() {
		// 校验契约负载与表字段一一对应的 builder 路径
		OperationLogPayload p = OperationLogPayload.builder()
			.userId(9L)
			.operationType("QUERY")
			.module("USER_ROLE")
			.targetType("User")
			.requestMethod("GET")
			.requestUri("/api/u")
			.requestParams("id=1")
			.responseStatus(200)
			.responseMessage("ok")
			.executionTimeMs(12)
			.ipAddress("127.0.0.1")
			.userAgent("JUnit")
			.className("C")
			.methodName("m")
			.build();

		assertEquals(9L, p.getUserId());
		assertEquals("QUERY", p.getOperationType());
		assertEquals("USER_ROLE", p.getModule());
		assertEquals("User", p.getTargetType());
		assertEquals(200, p.getResponseStatus());
		assertEquals("ok", p.getResponseMessage());
		assertNotNull(p.getTimestamp());
	}

}
