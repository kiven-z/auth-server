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

	@Test
	@DisplayName("批量释放超时 processing 占位")
	void cleanupStaleProcessingClaims_shouldDelegateToMapper() {
		// 调用运维清理接口
		when(authorizationInvalidationEventMapper.deleteStaleProcessingClaims(
				java.time.LocalDateTime.of(2026, 1, 1, 0, 0), 50)).thenReturn(3);

		int deleted = opsApplicationService.cleanupStaleProcessingClaims(java.time.LocalDateTime.of(2026, 1, 1, 0, 0),
				50);

		assertTrue(deleted == 3);
		verify(authorizationInvalidationEventMapper).deleteStaleProcessingClaims(
				java.time.LocalDateTime.of(2026, 1, 1, 0, 0), 50);
	}

	@Test
	@DisplayName("批量删除已完成过期幂等事件")
	void purgeCompletedBefore_shouldDelegateToMapper() {
		// 调用历史清理接口
		when(authorizationInvalidationEventMapper.deleteCompletedBefore(
				java.time.LocalDateTime.of(2025, 6, 1, 0, 0), 200)).thenReturn(10);

		int deleted = opsApplicationService.purgeCompletedBefore(java.time.LocalDateTime.of(2025, 6, 1, 0, 0), 200);

		assertTrue(deleted == 10);
		verify(authorizationInvalidationEventMapper).deleteCompletedBefore(
				java.time.LocalDateTime.of(2025, 6, 1, 0, 0), 200);
	}

}
