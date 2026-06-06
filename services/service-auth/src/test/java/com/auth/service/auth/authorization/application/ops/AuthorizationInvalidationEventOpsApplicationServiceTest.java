package com.auth.service.auth.authorization.application.ops;

import com.auth.service.auth.authorization.domain.invalidation.model.InvalidationProcessingMarker;
import com.auth.service.auth.authorization.domain.invalidation.port.InvalidationIdempotencyPort;
import com.auth.service.auth.authorization.infrastructure.persistence.AuthorizationInvalidationEventMapper;
import com.auth.service.auth.authorization.infrastructure.persistence.projection.AuthorizationInvalidationEventProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthorizationInvalidationEventOpsApplicationService} 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorizationInvalidationEventOpsApplicationService 幂等事件运维")
class AuthorizationInvalidationEventOpsApplicationServiceTest {

	@Mock
	private AuthorizationInvalidationEventMapper authorizationInvalidationEventMapper;

	@Mock
	private InvalidationIdempotencyPort invalidationIdempotencyPort;

	@InjectMocks
	private AuthorizationInvalidationEventOpsApplicationService opsApplicationService;

	@Test
	@DisplayName("无 processing 占位时不释放")
	void releaseProcessingClaim_shouldReturnFalseWhenNotProcessing() {
		AuthorizationInvalidationEventProjection completed = new AuthorizationInvalidationEventProjection();
		completed.setEventId("role:1");
		completed.setImpactedUserCount(3);
		when(authorizationInvalidationEventMapper.selectByEventId("role:1")).thenReturn(completed);

		boolean released = opsApplicationService.releaseProcessingClaim("role:1");

		assertFalse(released);
		verify(invalidationIdempotencyPort, never()).releaseClaim("role:1");
	}

	@Test
	@DisplayName("processing 占位时应调用 releaseClaim")
	void releaseProcessingClaim_shouldReleaseProcessingPlaceholder() {
		AuthorizationInvalidationEventProjection processing = new AuthorizationInvalidationEventProjection();
		processing.setEventId("role:2");
		processing.setImpactedUserCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		when(authorizationInvalidationEventMapper.selectByEventId("role:2")).thenReturn(processing);
		when(invalidationIdempotencyPort.releaseClaim("role:2")).thenReturn(true);

		boolean released = opsApplicationService.releaseProcessingClaim("role:2");

		assertTrue(released);
		verify(invalidationIdempotencyPort).releaseClaim("role:2");
	}

}
