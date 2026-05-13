package com.auth.service.system.log.service.impl;

import com.auth.module.security.contract.event.OperationLogPayload;
import com.auth.service.system.log.mapper.LogOperationMapper;
import com.auth.service.system.log.model.entity.LogOperationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

/**
 * {@link LogOperationAppendServiceImpl} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class LogOperationAppendServiceImplTest {

	@Mock
	private LogOperationMapper logOperationMapper;

	@Test
	@DisplayName("append 应插入与负载字段一致的实体并显式设置审计用户")
	void append_shouldInsertMappedEntity() {
		// 异步持久化线程无 SecurityContext，create_user 必须由负载显式带出
		LogOperationAppendServiceImpl service = new LogOperationAppendServiceImpl(logOperationMapper);
		OperationLogPayload payload = OperationLogPayload.builder()
			.userId(3L)
			.operationType("UPDATE")
			.module("USER_ROLE")
			.targetType("Role")
			.requestMethod("PUT")
			.requestUri("/api/roles/9")
			.requestParams("{}")
			.responseStatus(200)
			.responseMessage("ok")
			.executionTimeMs(5)
			.ipAddress("127.0.0.1")
			.userAgent("JUnit")
			.className("X")
			.methodName("y")
			.build();

		service.append(payload);

		ArgumentCaptor<LogOperationEntity> captor = ArgumentCaptor.forClass(LogOperationEntity.class);
		verify(logOperationMapper).insert(captor.capture());
		LogOperationEntity e = captor.getValue();
		assertEquals(3L, e.getUserId());
		assertEquals(3L, e.getCreateUser());
		assertEquals(3L, e.getUpdateUser());
		assertEquals("UPDATE", e.getOperationType());
		assertEquals("USER_ROLE", e.getModule());
		assertEquals("Role", e.getTargetType());
		assertEquals(200, e.getResponseStatus());
		assertEquals("ok", e.getResponseMessage());
		assertFalse(e.getIsDeleted());
	}

	@Test
	@DisplayName("append 应将仅空白的可选字符串字段规范为 null")
	void append_shouldNormalizeBlankOptionalStringsToNull() {
		LogOperationAppendServiceImpl service = new LogOperationAppendServiceImpl(logOperationMapper);
		OperationLogPayload payload = OperationLogPayload.builder()
			.userId(1L)
			.operationType("READ")
			.module("SYS")
			.targetType("   ")
			.requestMethod("GET")
			.requestUri("/x")
			.requestParams(" \t ")
			.responseStatus(200)
			.responseMessage("")
			.executionTimeMs(1)
			.ipAddress("127.0.0.1")
			.userAgent("")
			.className("C")
			.methodName("m")
			.build();

		service.append(payload);

		ArgumentCaptor<LogOperationEntity> captor = ArgumentCaptor.forClass(LogOperationEntity.class);
		verify(logOperationMapper).insert(captor.capture());
		LogOperationEntity e = captor.getValue();
		// 经 MapStruct + blankToNull 归一
		assertNull(e.getTargetType());
		assertNull(e.getRequestParams());
		assertNull(e.getResponseMessage());
		assertNull(e.getUserAgent());
	}

}
