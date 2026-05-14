package com.auth.module.security.contract.dto;

import com.auth.module.security.contract.event.OperationLogPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link OperationLogIngestRequest} 与 {@link OperationLogPayload} 互转单元测试
 *
 * @author Bunny
 */
class OperationLogIngestRequestTest {

	@Test
	@DisplayName("fromPayload 与 toPayload 应保留 targetId")
	void roundTrip_shouldPreserveTargetId() {
		OperationLogPayload original = OperationLogPayload.builder()
			.userId(1L)
			.operationType("QUERY")
			.module("SYSTEM:X")
			.targetType("Dept")
			.targetId(55L)
			.requestMethod("GET")
			.requestUri("/d/55")
			.requestParams(null)
			.responseStatus(200)
			.responseMessage(null)
			.executionTimeMs(3)
			.ipAddress("127.0.0.1")
			.userAgent(null)
			.className("C")
			.methodName("get")
			.build();

		OperationLogIngestRequest req = OperationLogIngestRequest.fromPayload(original);
		assertEquals(55L, req.getTargetId());

		OperationLogPayload back = req.toPayload();
		assertEquals(55L, back.getTargetId());
	}

	@Test
	@DisplayName("targetId 为空时应保持 null")
	void roundTrip_shouldAllowNullTargetId() {
		OperationLogPayload original = OperationLogPayload.builder()
			.userId(1L)
			.operationType("QUERY")
			.module("SYSTEM:X")
			.targetType("Dept")
			.requestMethod("GET")
			.requestUri("/d")
			.requestParams(null)
			.responseStatus(200)
			.responseMessage(null)
			.executionTimeMs(3)
			.ipAddress("127.0.0.1")
			.userAgent(null)
			.className("C")
			.methodName("list")
			.build();

		OperationLogIngestRequest req = OperationLogIngestRequest.fromPayload(original);
		assertNull(req.getTargetId());
		assertNull(req.toPayload().getTargetId());
	}

}
