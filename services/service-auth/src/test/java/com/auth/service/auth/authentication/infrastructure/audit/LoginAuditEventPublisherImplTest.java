package com.auth.service.auth.authentication.infrastructure.audit;

import com.auth.service.auth.authentication.domain.enums.AuthLoginLogResult;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.event.LoginAuditEvent;
import com.auth.service.auth.authentication.domain.model.LoginAuditSnapshot;
import com.auth.service.auth.authentication.domain.port.LoginAuditSnapshotPort;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link LoginAuditEventPublisherImpl} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class LoginAuditEventPublisherImplTest {

	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	@Mock
	private LoginAuditSnapshotPort loginAuditSnapshotPort;

	@Mock
	private HttpServletRequest httpServletRequest;

	@Test
	@DisplayName("发布时应附带 Port 解析的快照")
	void publish_shouldEmitEventWithSnapshotFromPort() {
		LoginAuditSnapshot snapshot = LoginAuditSnapshot.empty();
		when(loginAuditSnapshotPort.build(httpServletRequest)).thenReturn(snapshot);

		LoginAuditEventPublisherImpl publisher = new LoginAuditEventPublisherImpl(applicationEventPublisher,
				loginAuditSnapshotPort);
		publisher.publish(httpServletRequest, AuthLoginLogType.LOGIN_PASSWORD, AuthLoginLogResult.SUCCESS, 9L, "user",
				null, "jti-x");

		ArgumentCaptor<LoginAuditEvent> captor = ArgumentCaptor.forClass(LoginAuditEvent.class);
		verify(applicationEventPublisher).publishEvent(captor.capture());
		LoginAuditEvent event = captor.getValue();
		assertSame(snapshot, event.snapshot());
		assertEquals(AuthLoginLogType.LOGIN_PASSWORD, event.eventType());
		assertEquals(AuthLoginLogResult.SUCCESS, event.result());
		assertEquals(9L, event.userId());
		assertEquals("user", event.principal());
		assertEquals("jti-x", event.sessionId());
	}

}
