package com.auth.service.auth.authorization.infrastructure.persistence.adapter;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.authorization.domain.invalidation.model.IdempotencyClaimStatus;
import com.auth.service.auth.authorization.domain.invalidation.model.InvalidationProcessingMarker;
import com.auth.service.auth.authorization.infrastructure.persistence.AuthorizationInvalidationEventMapper;
import com.auth.service.auth.authorization.infrastructure.persistence.projection.AuthorizationInvalidationEventProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    @DisplayName("已完成事件不返回处理中占位")
    void findProcessedOutcome_processingRow_shouldBeEmpty() {
        AuthorizationInvalidationEventProjection processing = new AuthorizationInvalidationEventProjection();
        processing.setImpactedUserCount(InvalidationProcessingMarker.PROCESSING_COUNT);
        when(authorizationInvalidationEventMapper.selectByEventId("evt-p")).thenReturn(processing);

        assertTrue(adapter.findProcessedOutcome("evt-p").isEmpty());
    }

    @Test
    @DisplayName("tryClaim 成功插入占位行")
    void tryClaim_insertSuccess_shouldReturnClaimed() {
        when(authorizationInvalidationEventMapper.selectByEventId("evt-1")).thenReturn(null);
        when(authorizationInvalidationEventMapper.insertProcessingClaim(any())).thenReturn(1);

        IdempotencyClaimStatus status = adapter.tryClaim("evt-1", AuthorizationChangeKind.ROLE);

        assertEquals(IdempotencyClaimStatus.CLAIMED, status);
        ArgumentCaptor<AuthorizationInvalidationEventProjection> captor = ArgumentCaptor
                .forClass(AuthorizationInvalidationEventProjection.class);
        verify(authorizationInvalidationEventMapper).insertProcessingClaim(captor.capture());
        assertEquals(InvalidationProcessingMarker.PROCESSING_COUNT, captor.getValue().getImpactedUserCount());
    }

    @Test
    @DisplayName("tryClaim 唯一键冲突且仍为处理中")
    void tryClaim_duplicateProcessing_shouldReturnInProgress() {
        when(authorizationInvalidationEventMapper.selectByEventId("evt-2")).thenReturn(null);
        when(authorizationInvalidationEventMapper.insertProcessingClaim(any()))
                .thenThrow(new DuplicateKeyException("dup"));

        AuthorizationInvalidationEventProjection processing = new AuthorizationInvalidationEventProjection();
        processing.setImpactedUserCount(InvalidationProcessingMarker.PROCESSING_COUNT);
        when(authorizationInvalidationEventMapper.selectByEventId("evt-2")).thenReturn(null, processing);

        IdempotencyClaimStatus status = adapter.tryClaim("evt-2", AuthorizationChangeKind.ROLE);

        assertEquals(IdempotencyClaimStatus.IN_PROGRESS, status);
    }

    @Test
    @DisplayName("completeProcessedOutcome 更新占位行")
    void completeProcessedOutcome_shouldUpdateRow() {
        AuthorizationInvalidateResponse response = new AuthorizationInvalidateResponse(1, 1, 1, 0);
        when(authorizationInvalidationEventMapper.updateProcessedOutcome(any())).thenReturn(1);

        adapter.completeProcessedOutcome("evt-3", AuthorizationChangeKind.ROLE, response);

        verify(authorizationInvalidationEventMapper).updateProcessedOutcome(any());
    }

}
