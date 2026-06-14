package com.auth.service.auth.infrastructure.persistence.adapter;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.domain.authorization.invalidation.model.InvalidationIdempotencyGate;
import com.auth.service.auth.domain.authorization.invalidation.model.InvalidationProcessingMarker;
import com.auth.service.auth.infrastructure.persistence.AuthorizationInvalidationEventMapper;
import com.auth.service.auth.infrastructure.persistence.projection.AuthorizationInvalidationEventProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link InvalidationIdempotencyAdapter} 单元测试
 */
@DisplayName("InvalidationIdempotencyAdapter 幂等适配器")
@ExtendWith(MockitoExtension.class)
class InvalidationIdempotencyAdapterTest {

	@Mock
	private AuthorizationInvalidationEventMapper authorizationInvalidationEventMapper;

	@InjectMocks
	private InvalidationIdempotencyAdapter adapter;

	@Test
	@DisplayName("已完成事件直接返回 Completed")
	void acquireGate_completedRow_shouldReturnCompleted() {
		AuthorizationInvalidationEventProjection completed = new AuthorizationInvalidationEventProjection();
		completed.setImpactedUserCount(10);
		completed.setVersionBumpedCount(8);
		completed.setProfileRefreshedCount(8);
		completed.setProfileEvictedCount(2);
		when(authorizationInvalidationEventMapper.selectByEventId("evt-done")).thenReturn(completed);

		InvalidationIdempotencyGate gate = adapter.acquireGate("evt-done", AuthorizationChangeKind.ROLE);

		assertInstanceOf(InvalidationIdempotencyGate.Completed.class, gate);
		assertEquals(new AuthorizationInvalidateResponse(10, 8, 8, 2),
				((InvalidationIdempotencyGate.Completed) gate).response());
	}

	@Test
	@DisplayName("acquireGate 成功插入占位行")
	void acquireGate_insertSuccess_shouldReturnClaimed() {
		when(authorizationInvalidationEventMapper.selectByEventId("evt-1")).thenReturn(null);
		when(authorizationInvalidationEventMapper.insertProcessingClaim(any())).thenReturn(1);

		InvalidationIdempotencyGate gate = adapter.acquireGate("evt-1", AuthorizationChangeKind.ROLE);

		assertInstanceOf(InvalidationIdempotencyGate.Claimed.class, gate);
		ArgumentCaptor<AuthorizationInvalidationEventProjection> captor = ArgumentCaptor
			.forClass(AuthorizationInvalidationEventProjection.class);
		verify(authorizationInvalidationEventMapper).insertProcessingClaim(captor.capture());
		assertEquals(InvalidationProcessingMarker.PROCESSING_COUNT, captor.getValue().getImpactedUserCount());
	}

	@Test
	@DisplayName("acquireGate 唯一键冲突且仍为处理中")
	void acquireGate_duplicateProcessing_shouldReturnInProgress() {
		when(authorizationInvalidationEventMapper.selectByEventId("evt-2")).thenReturn(null);
		when(authorizationInvalidationEventMapper.insertProcessingClaim(any()))
			.thenThrow(new DuplicateKeyException("dup"));

		AuthorizationInvalidationEventProjection processing = new AuthorizationInvalidationEventProjection();
		processing.setImpactedUserCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		when(authorizationInvalidationEventMapper.selectByEventId("evt-2")).thenReturn(null, processing);

		InvalidationIdempotencyGate gate = adapter.acquireGate("evt-2", AuthorizationChangeKind.ROLE);

		assertInstanceOf(InvalidationIdempotencyGate.InProgress.class, gate);
	}

	@Test
	@DisplayName("acquireGate 唯一键冲突且对方已完成")
	void acquireGate_duplicateCompleted_shouldReturnCompleted() {
		AuthorizationInvalidationEventProjection processing = new AuthorizationInvalidationEventProjection();
		processing.setImpactedUserCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		when(authorizationInvalidationEventMapper.selectByEventId("evt-3")).thenReturn(processing);
		when(authorizationInvalidationEventMapper.insertProcessingClaim(any()))
			.thenThrow(new DuplicateKeyException("dup"));

		AuthorizationInvalidationEventProjection completed = new AuthorizationInvalidationEventProjection();
		completed.setImpactedUserCount(3);
		completed.setVersionBumpedCount(2);
		completed.setProfileRefreshedCount(2);
		completed.setProfileEvictedCount(1);
		when(authorizationInvalidationEventMapper.selectByEventId("evt-3")).thenReturn(processing, completed);

		InvalidationIdempotencyGate gate = adapter.acquireGate("evt-3", AuthorizationChangeKind.ROLE);

		assertInstanceOf(InvalidationIdempotencyGate.Completed.class, gate);
		assertEquals(new AuthorizationInvalidateResponse(3, 2, 2, 1),
				((InvalidationIdempotencyGate.Completed) gate).response());
	}

	@Test
	@DisplayName("completeProcessedOutcome 更新占位行")
	void completeProcessedOutcome_shouldUpdateRow() {
		AuthorizationInvalidateResponse response = new AuthorizationInvalidateResponse(1, 1, 1, 0);
		when(authorizationInvalidationEventMapper.updateProcessedOutcome(any())).thenReturn(1);

		adapter.completeProcessedOutcome("evt-4", AuthorizationChangeKind.ROLE, response);

		verify(authorizationInvalidationEventMapper).updateProcessedOutcome(any());
	}

}
