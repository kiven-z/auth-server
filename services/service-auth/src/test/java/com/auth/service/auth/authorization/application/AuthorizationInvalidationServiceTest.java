package com.auth.service.auth.authorization.application;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.module.security.contract.dto.invalidation.RoleInvalidatePayload;
import com.auth.service.auth.authorization.application.invalidation.impact.ImpactResolverRegistry;
import com.auth.service.auth.authorization.application.invalidation.policy.UserInvalidationPolicy;
import com.auth.service.auth.authorization.config.AuthorizationInvalidationProperties;
import com.auth.service.auth.authorization.domain.invalidation.model.UserInvalidationBuckets;
import com.auth.service.auth.authorization.domain.invalidation.model.UserInvalidationState;
import com.auth.service.auth.authorization.domain.invalidation.port.InvalidationIdempotencyPort;
import com.auth.service.auth.authorization.domain.invalidation.port.UserInvalidationStateQueryPort;
import com.auth.service.auth.authorization.domain.invalidation.port.UserPermVersionPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AuthorizationInvalidationService} 单元测试
 */
@DisplayName("AuthorizationInvalidationService 授权失效编排")
@ExtendWith(MockitoExtension.class)
class AuthorizationInvalidationServiceTest {

	@Mock
	private InvalidationIdempotencyPort invalidationIdempotencyPort;

	@Mock
	private ImpactResolverRegistry impactResolverRegistry;

	@Mock
	private UserInvalidationStateQueryPort userInvalidationStateQueryPort;

	@Mock
	private UserInvalidationPolicy userInvalidationPolicy;

	@Mock
	private UserPermVersionPort userPermVersionPort;

	@Mock
	private AuthProfileMaterializationService authProfileMaterializationService;

	@Mock
	private AuthorizationInvalidationProperties authorizationInvalidationProperties;

	@InjectMocks
	private AuthorizationInvalidationService authorizationInvalidationService;

	@Test
	@DisplayName("幂等命中时直接返回缓存结果")
	void invalidate_idempotentHit_shouldReturnCached() {
		AuthorizationInvalidateResponse cached = new AuthorizationInvalidateResponse(10, 8, 8, 2);
		when(invalidationIdempotencyPort.findProcessedOutcome("evt-1")).thenReturn(Optional.of(cached));

		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest("evt-1",
				AuthorizationChangeKind.ROLE, new RoleInvalidatePayload(List.of("ADMIN")));
		AuthorizationInvalidateResponse response = authorizationInvalidationService.invalidate(request);

		assertEquals(cached, response);
		verify(impactResolverRegistry, never()).resolve(any());
	}

	@Test
	@DisplayName("无影响用户时返回全零并记录幂等")
	void invalidate_emptyImpact_shouldReturnEmpty() {
		when(invalidationIdempotencyPort.findProcessedOutcome("evt-2")).thenReturn(Optional.empty());
		when(impactResolverRegistry.resolve(any())).thenReturn(Set.of());

		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest("evt-2",
				AuthorizationChangeKind.ROLE, new RoleInvalidatePayload(List.of("ADMIN")));
		AuthorizationInvalidateResponse response = authorizationInvalidationService.invalidate(request);

		assertEquals(AuthorizationInvalidateResponse.empty(), response);
		verify(invalidationIdempotencyPort).saveProcessedOutcome(eq("evt-2"), eq(AuthorizationChangeKind.ROLE),
				eq(AuthorizationInvalidateResponse.empty()));
	}

	@Test
	@DisplayName("先递增 perm_version 再刷新画像")
	void invalidate_shouldIncrementBeforeRefresh() {
		when(invalidationIdempotencyPort.findProcessedOutcome("evt-3")).thenReturn(Optional.empty());
		when(impactResolverRegistry.resolve(any())).thenReturn(Set.of(1L));
		when(userInvalidationStateQueryPort.loadByUserIds(Set.of(1L)))
			.thenReturn(List.of(new UserInvalidationState(1L, 1, false)));
		when(userInvalidationPolicy.partition(anyCollection()))
			.thenReturn(new UserInvalidationBuckets(Set.of(1L), Set.of()));
		when(authorizationInvalidationProperties.getBatchSize()).thenReturn(500);
		when(userPermVersionPort.incrementInBatches(Set.of(1L), 500)).thenReturn(1);
		when(authProfileMaterializationService.refreshInBatches(Set.of(1L), 500)).thenReturn(1);
		when(authProfileMaterializationService.evictInBatches(Set.of(), 500)).thenReturn(0);

		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest("evt-3",
				AuthorizationChangeKind.ROLE, new RoleInvalidatePayload(List.of("ADMIN")));
		AuthorizationInvalidateResponse response = authorizationInvalidationService.invalidate(request);

		assertEquals(new AuthorizationInvalidateResponse(1, 1, 1, 0), response);

		InOrder order = inOrder(userPermVersionPort, authProfileMaterializationService);
		order.verify(userPermVersionPort).incrementInBatches(Set.of(1L), 500);
		order.verify(authProfileMaterializationService).refreshInBatches(Set.of(1L), 500);
		verify(invalidationIdempotencyPort).saveProcessedOutcome(eq("evt-3"), eq(AuthorizationChangeKind.ROLE),
				eq(new AuthorizationInvalidateResponse(1, 1, 1, 0)));
	}

}
