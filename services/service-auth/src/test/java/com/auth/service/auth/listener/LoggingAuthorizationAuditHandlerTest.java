package com.auth.service.auth.listener;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.dto.AuthorizationAuditIngestRequest;
import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.module.security.contract.event.SecurityEventType;
import com.auth.service.auth.feign.SystemAuthorizationAuditFeignClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link LoggingAuthorizationAuditHandler} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class LoggingAuthorizationAuditHandlerTest {

	@Mock
	private SystemAuthorizationAuditFeignClient systemAuthorizationAuditFeignClient;

	@Test
	@DisplayName("handle 应通过 Feign 上报与事件一致的负载")
	void handle_shouldInvokeFeignWithMappedBody() {
		when(systemAuthorizationAuditFeignClient.appendRecord(any())).thenReturn(Result.success());
		LoggingAuthorizationAuditHandler handler = new LoggingAuthorizationAuditHandler(
				systemAuthorizationAuditFeignClient);
		SecurityAuthorizationAuditPayloadEvent event = SecurityAuthorizationAuditPayloadEvent.builder()
			.eventType(SecurityEventType.GRANTED)
			.userId(3L)
			.requestUri("/api/z")
			.build();

		handler.handle(event);

		ArgumentCaptor<AuthorizationAuditIngestRequest> captor = ArgumentCaptor
			.forClass(AuthorizationAuditIngestRequest.class);
		verify(systemAuthorizationAuditFeignClient).appendRecord(captor.capture());
		AuthorizationAuditIngestRequest body = captor.getValue();
		assertEquals(SecurityEventType.GRANTED, body.getEventType());
		assertEquals(3L, body.getUserId());
		assertEquals("/api/z", body.getRequestUri());
	}

	@Test
	@DisplayName("Feign 抛出异常时不应向外抛出")
	void handle_whenFeignThrows_shouldSwallow() {
		when(systemAuthorizationAuditFeignClient.appendRecord(any())).thenThrow(new IllegalStateException("network"));
		LoggingAuthorizationAuditHandler handler = new LoggingAuthorizationAuditHandler(
				systemAuthorizationAuditFeignClient);
		SecurityAuthorizationAuditPayloadEvent event = SecurityAuthorizationAuditPayloadEvent.builder()
			.userId(1L)
			.requestUri("/u")
			.build();

		assertDoesNotThrow(() -> handler.handle(event));
	}

	@Test
	@DisplayName("Feign 返回非成功码时不应向外抛出")
	void handle_whenResultNonSuccess_shouldSwallow() {
		when(systemAuthorizationAuditFeignClient.appendRecord(any())).thenReturn(Result.error("bad"));
		LoggingAuthorizationAuditHandler handler = new LoggingAuthorizationAuditHandler(
				systemAuthorizationAuditFeignClient);
		SecurityAuthorizationAuditPayloadEvent event = SecurityAuthorizationAuditPayloadEvent.builder()
			.userId(2L)
			.requestUri("/v")
			.build();

		assertDoesNotThrow(() -> handler.handle(event));
	}

}
