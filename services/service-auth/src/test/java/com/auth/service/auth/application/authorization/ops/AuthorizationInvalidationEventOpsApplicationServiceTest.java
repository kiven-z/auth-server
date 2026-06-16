package com.auth.service.auth.application.authorization.ops;

import com.auth.service.auth.application.port.invalidation.InvalidationEventOpsPort;
import com.auth.service.auth.application.port.invalidation.InvalidationIdempotencyPort;
import com.auth.service.auth.domain.authorization.invalidation.InvalidationProcessingMarker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link AuthorizationInvalidationEventOpsApplicationService} 单元测试
 */
@DisplayName("AuthorizationInvalidationEventOpsApplicationService 幂等事件运维")
@ExtendWith(MockitoExtension.class)
class AuthorizationInvalidationEventOpsApplicationServiceTest {

	@Mock
	private InvalidationEventOpsPort invalidationEventOpsPort;

	@Mock
	private InvalidationIdempotencyPort invalidationIdempotencyPort;

	@InjectMocks
	private AuthorizationInvalidationEventOpsApplicationService opsApplicationService;

	@Test
	@DisplayName("无 processing 占位时不释放")
	void releaseProcessingClaim_shouldReturnFalseWhenNotProcessing() {
		when(invalidationEventOpsPort.findImpactedUserCountByEventId("role:1")).thenReturn(3);

		boolean released = opsApplicationService.releaseProcessingClaim("role:1");

		assertFalse(released);
		verify(invalidationIdempotencyPort, never()).releaseClaim("role:1");
	}

	@Test
	@DisplayName("processing 占位时应调用 releaseClaim")
	void releaseProcessingClaim_shouldReleaseProcessingPlaceholder() {
		when(invalidationEventOpsPort.findImpactedUserCountByEventId("role:2"))
			.thenReturn(InvalidationProcessingMarker.PROCESSING_COUNT);
		when(invalidationIdempotencyPort.releaseClaim("role:2")).thenReturn(true);

		boolean released = opsApplicationService.releaseProcessingClaim("role:2");

		assertTrue(released);
		verify(invalidationIdempotencyPort).releaseClaim("role:2");
	}

	@Test
	@DisplayName("批量释放超时 processing 占位")
	void cleanupStaleProcessingClaims_shouldDelegateToPort() {
		when(invalidationEventOpsPort.deleteStaleProcessingClaims(java.time.LocalDateTime.of(2026, 1, 1, 0, 0), 50))
			.thenReturn(3);

		int deleted = opsApplicationService.cleanupStaleProcessingClaims(java.time.LocalDateTime.of(2026, 1, 1, 0, 0),
				50);

		assertEquals(3, deleted);
		verify(invalidationEventOpsPort).deleteStaleProcessingClaims(java.time.LocalDateTime.of(2026, 1, 1, 0, 0), 50);
	}

	@Test
	@DisplayName("批量删除已完成过期幂等事件")
	void purgeCompletedBefore_shouldDelegateToPort() {
		when(invalidationEventOpsPort.deleteCompletedBefore(java.time.LocalDateTime.of(2025, 6, 1, 0, 0), 200))
			.thenReturn(10);

		int deleted = opsApplicationService.purgeCompletedBefore(java.time.LocalDateTime.of(2025, 6, 1, 0, 0), 200);

		assertEquals(10, deleted);
		verify(invalidationEventOpsPort).deleteCompletedBefore(java.time.LocalDateTime.of(2025, 6, 1, 0, 0), 200);
	}

}
