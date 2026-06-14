package com.auth.service.auth.application.authorization.ops;

import com.auth.service.auth.application.port.invalidation.InvalidationIdempotencyPort;
import com.auth.service.auth.domain.authorization.invalidation.model.InvalidationProcessingMarker;
import com.auth.service.auth.infrastructure.persistence.AuthorizationInvalidationEventMapper;
import com.auth.service.auth.infrastructure.persistence.projection.AuthorizationInvalidationEventProjection;
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

	@Test
	@DisplayName("批量释放超时 processing 占位")
	void cleanupStaleProcessingClaims_shouldDelegateToMapper() {
		// 调用运维清理接口
		when(authorizationInvalidationEventMapper
			.deleteStaleProcessingClaims(java.time.LocalDateTime.of(2026, 1, 1, 0, 0), 50)).thenReturn(3);

		int deleted = opsApplicationService.cleanupStaleProcessingClaims(java.time.LocalDateTime.of(2026, 1, 1, 0, 0),
				50);

		assertEquals(3, deleted);
		verify(authorizationInvalidationEventMapper)
			.deleteStaleProcessingClaims(java.time.LocalDateTime.of(2026, 1, 1, 0, 0), 50);
	}

	@Test
	@DisplayName("批量删除已完成过期幂等事件")
	void purgeCompletedBefore_shouldDelegateToMapper() {
		// 调用历史清理接口
		when(authorizationInvalidationEventMapper.deleteCompletedBefore(java.time.LocalDateTime.of(2025, 6, 1, 0, 0),
				200))
			.thenReturn(10);

		int deleted = opsApplicationService.purgeCompletedBefore(java.time.LocalDateTime.of(2025, 6, 1, 0, 0), 200);

		assertEquals(10, deleted);
		verify(authorizationInvalidationEventMapper).deleteCompletedBefore(java.time.LocalDateTime.of(2025, 6, 1, 0, 0),
				200);
	}

}
