package com.auth.service.system.log.service.impl;

import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.module.security.contract.event.SecurityEventType;
import com.auth.service.system.log.mapper.LogAuthorizationAuditMapper;
import com.auth.service.system.log.model.entity.LogAuthorizationAuditEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

/**
 * {@link LogAuthorizationAuditAppendServiceImpl} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class LogAuthorizationAuditAppendServiceImplTest {

	@Mock
	private LogAuthorizationAuditMapper logAuthorizationAuditMapper;

	@Test
	@DisplayName("append 应插入与事件字段一致的实体")
	void append_shouldInsertMappedEntity() {
		LogAuthorizationAuditAppendServiceImpl service = new LogAuthorizationAuditAppendServiceImpl(
				logAuthorizationAuditMapper);
		SecurityAuthorizationAuditPayloadEvent event = SecurityAuthorizationAuditPayloadEvent.builder()
			.eventType(SecurityEventType.DENIED)
			.userId(7L)
			.requestMethod("POST")
			.requestUri("/r")
			.requestIp("127.0.0.1")
			.requiredAuthority("p:1")
			.decisionReason("NO_MATCH")
			.exceptionMessage("x")
			.className("A")
			.methodName("b")
			.build();

		service.append(event);

		ArgumentCaptor<LogAuthorizationAuditEntity> captor = ArgumentCaptor.forClass(LogAuthorizationAuditEntity.class);
		verify(logAuthorizationAuditMapper).insert(captor.capture());
		LogAuthorizationAuditEntity e = captor.getValue();
		assertEquals(SecurityEventType.DENIED.name(), e.getEventType());
		assertEquals(7L, e.getCreateUser());
		assertEquals("POST", e.getRequestMethod());
		assertEquals("/r", e.getRequestUri());
		assertEquals("127.0.0.1", e.getRequestIp());
		assertEquals("p:1", e.getRequiredPermission());
		assertEquals("NO_MATCH", e.getDecisionReason());
		assertEquals("A", e.getClassName());
		assertEquals("b", e.getMethodName());
	}

}
