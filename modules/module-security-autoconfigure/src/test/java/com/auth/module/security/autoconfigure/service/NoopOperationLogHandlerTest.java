package com.auth.module.security.autoconfigure.service;

import com.auth.module.security.contract.event.OperationLogPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * {@link NoopOperationLogHandler} 单元测试
 *
 * @author Bunny
 */
class NoopOperationLogHandlerTest {

	@Test
	@DisplayName("handle 应对任意负载静默成功")
	void handle_shouldNotThrow() {
		NoopOperationLogHandler handler = new NoopOperationLogHandler();
		OperationLogPayload payload = OperationLogPayload.builder()
			.userId(1L)
			.operationType("QUERY")
			.module("M")
			.targetType("")
			.requestMethod("GET")
			.requestUri("/")
			.requestParams(null)
			.responseStatus(200)
			.responseMessage(null)
			.executionTimeMs(1)
			.ipAddress("127.0.0.1")
			.userAgent(null)
			.className("A")
			.methodName("b")
			.build();
		// 空实现仅用于未接入持久化时的兜底，不应抛出异常
		assertDoesNotThrow(() -> handler.handle(payload));
	}

}
