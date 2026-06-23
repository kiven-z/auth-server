package com.auth.service.auth.application.authorization.command;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.module.security.contract.dto.invalidation.RoleInvalidatePayload;
import com.auth.service.auth.application.authorization.invalidation.impact.ImpactResolverRegistry;
import com.auth.service.auth.application.authorization.invalidation.policy.UserInvalidationPolicy;
import com.auth.service.auth.application.port.invalidation.AuthorizationInvalidationEventPort;
import com.auth.service.auth.application.port.invalidation.UserInvalidationStatePort;
import com.auth.service.auth.config.AuthorizationInvalidationProperties;
import com.auth.service.auth.domain.authorization.model.InvalidationIdempotencyGate;
import com.auth.service.auth.domain.authorization.model.UserInvalidationBuckets;
import com.auth.service.auth.domain.authorization.model.UserInvalidationState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

/**
 * {@link AuthorizationInvalidationService} 单元测试
 */
@DisplayName("AuthorizationInvalidationService 授权失效编排")
@ExtendWith(MockitoExtension.class)
class AuthorizationInvalidationServiceTest {

	@Mock
	private AuthorizationInvalidationEventPort authorizationInvalidationEventPort;

	@Mock
	private ImpactResolverRegistry impactResolverRegistry;

	@Mock
	private UserInvalidationStatePort userInvalidationStatePort;

	@Mock
	private UserInvalidationPolicy userInvalidationPolicy;

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
		when(authorizationInvalidationEventPort.acquireGate("evt-1", AuthorizationChangeKind.ROLE))
			.thenReturn(new InvalidationIdempotencyGate.Completed(cached));

		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest("evt-1",
				AuthorizationChangeKind.ROLE, new RoleInvalidatePayload(List.of("ADMIN")));
		AuthorizationInvalidateResponse response = authorizationInvalidationService.invalidate(request);

		assertEquals(cached, response);
		verify(impactResolverRegistry, never()).resolve(any());
	}

	@Test
	@DisplayName("无影响用户时返回全零并完成幂等占位")
	void invalidate_emptyImpact_shouldReturnEmpty() {
		when(authorizationInvalidationEventPort.acquireGate("evt-2", AuthorizationChangeKind.ROLE))
			.thenReturn(new InvalidationIdempotencyGate.Claimed());
		when(impactResolverRegistry.resolve(any())).thenReturn(Set.of());

		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest("evt-2",
				AuthorizationChangeKind.ROLE, new RoleInvalidatePayload(List.of("ADMIN")));
		AuthorizationInvalidateResponse response = authorizationInvalidationService.invalidate(request);

		assertEquals(AuthorizationInvalidateResponse.empty(), response);
		verify(authorizationInvalidationEventPort).completeProcessedOutcome("evt-2", AuthorizationChangeKind.ROLE,
				AuthorizationInvalidateResponse.empty());
	}

	@Test
	@DisplayName("先递增 perm_version 再刷新画像")
	void invalidate_shouldIncrementBeforeRefresh() {
		when(authorizationInvalidationEventPort.acquireGate("evt-3", AuthorizationChangeKind.ROLE))
			.thenReturn(new InvalidationIdempotencyGate.Claimed());
		when(impactResolverRegistry.resolve(any())).thenReturn(Set.of(1L));
		when(userInvalidationStatePort.loadByUserIds(Set.of(1L))).thenReturn(List.of(new UserInvalidationState(1L, 1)));
		when(userInvalidationPolicy.partition(anyCollection()))
			.thenReturn(new UserInvalidationBuckets(Set.of(1L), Set.of()));
		when(authorizationInvalidationProperties.getBatchSize()).thenReturn(500);
		when(userInvalidationStatePort.incrementPermVersionInBatches(Set.of(1L), 500)).thenReturn(1);
		when(authProfileMaterializationService.refreshInBatches(Set.of(1L), 500)).thenReturn(1);
		when(authProfileMaterializationService.evictInBatches(Set.of(), 500)).thenReturn(0);

		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest("evt-3",
				AuthorizationChangeKind.ROLE, new RoleInvalidatePayload(List.of("ADMIN")));
		AuthorizationInvalidateResponse response = authorizationInvalidationService.invalidate(request);

		assertEquals(new AuthorizationInvalidateResponse(1, 1, 1, 0), response);

		InOrder order = inOrder(userInvalidationStatePort, authProfileMaterializationService);
		order.verify(userInvalidationStatePort).incrementPermVersionInBatches(Set.of(1L), 500);
		order.verify(authProfileMaterializationService).refreshInBatches(Set.of(1L), 500);
		verify(authorizationInvalidationEventPort).completeProcessedOutcome("evt-3", AuthorizationChangeKind.ROLE,
				new AuthorizationInvalidateResponse(1, 1, 1, 0));
	}

	@Test
	@DisplayName("流水线失败时释放幂等占位")
	void invalidate_pipelineFailure_shouldReleaseClaim() {
		when(authorizationInvalidationEventPort.acquireGate("evt-5", AuthorizationChangeKind.ROLE))
			.thenReturn(new InvalidationIdempotencyGate.Claimed());
		when(impactResolverRegistry.resolve(any())).thenThrow(new IllegalStateException("resolver error"));

		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest("evt-5",
				AuthorizationChangeKind.ROLE, new RoleInvalidatePayload(List.of("ADMIN")));

		try {
			authorizationInvalidationService.invalidate(request);
		}
		catch (IllegalStateException ignored) {
			// expected
		}

		verify(authorizationInvalidationEventPort).releaseClaim("evt-5");
		verify(authorizationInvalidationEventPort, never()).completeProcessedOutcome(any(), any(), any());
	}

	@Test
	@DisplayName("他人处理中时返回空结果")
	void invalidate_inProgress_shouldReturnEmpty() {
		when(authorizationInvalidationEventPort.acquireGate("evt-6", AuthorizationChangeKind.ROLE))
			.thenReturn(new InvalidationIdempotencyGate.InProgress());

		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest("evt-6",
				AuthorizationChangeKind.ROLE, new RoleInvalidatePayload(List.of("ADMIN")));
		AuthorizationInvalidateResponse response = authorizationInvalidationService.invalidate(request);

		assertEquals(AuthorizationInvalidateResponse.empty(), response);
		verify(impactResolverRegistry, never()).resolve(any());
	}

}
