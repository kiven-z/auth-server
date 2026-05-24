package com.auth.service.auth.authorization.application;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.authorization.application.invalidation.impact.ImpactResolverRegistry;
import com.auth.service.auth.authorization.application.invalidation.policy.UserInvalidationPolicy;
import com.auth.service.auth.authorization.config.AuthorizationInvalidationProperties;
import com.auth.service.auth.authorization.domain.invalidation.model.IdempotencyClaimStatus;
import com.auth.service.auth.authorization.domain.invalidation.model.InvalidationCommand;
import com.auth.service.auth.authorization.domain.invalidation.model.UserInvalidationBuckets;
import com.auth.service.auth.authorization.domain.invalidation.model.UserInvalidationState;
import com.auth.service.auth.authorization.domain.invalidation.port.InvalidationIdempotencyPort;
import com.auth.service.auth.authorization.domain.invalidation.port.UserInvalidationStateQueryPort;
import com.auth.service.auth.authorization.domain.invalidation.port.UserPermVersionPort;
import com.auth.service.auth.session.application.SessionAdminApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 授权失效编排唯一入口：反查 → 分桶 → 递增版本 → 刷新/驱逐画像。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AuthorizationInvalidationService {

	private final InvalidationIdempotencyPort invalidationIdempotencyPort;

	private final ImpactResolverRegistry impactResolverRegistry;

	private final UserInvalidationStateQueryPort userInvalidationStateQueryPort;

	private final UserInvalidationPolicy userInvalidationPolicy;

	private final UserPermVersionPort userPermVersionPort;

	private final AuthProfileMaterializationService authProfileMaterializationService;

	private final SessionAdminApplicationService sessionAdminApplicationService;

	private final AuthorizationInvalidationProperties authorizationInvalidationProperties;

	/**
	 * 执行授权失效流水线。
	 * @param request 统一失效请求
	 * @return 执行结果摘要
	 */
	public AuthorizationInvalidateResponse invalidate(AuthorizationInvalidateRequest request) {
		InvalidationCommand command = new InvalidationCommand(request.eventId(), request.payload());
		String eventId = command.eventId();

		Optional<AuthorizationInvalidateResponse> cached = invalidationIdempotencyPort.findProcessedOutcome(eventId);
		if (cached.isPresent()) {
			log.debug("Authorization invalidate skipped (idempotent): eventId={}", eventId);
			return cached.get();
		}

		IdempotencyClaimStatus claimStatus = invalidationIdempotencyPort.tryClaim(eventId, request.kind());
		if (claimStatus == IdempotencyClaimStatus.ALREADY_COMPLETED) {
			return invalidationIdempotencyPort.findProcessedOutcome(eventId)
				.orElse(AuthorizationInvalidateResponse.empty());
		}
		if (claimStatus == IdempotencyClaimStatus.IN_PROGRESS) {
			log.warn("Authorization invalidate in progress by another worker: eventId={}", eventId);
			return invalidationIdempotencyPort.findProcessedOutcome(eventId)
				.orElse(AuthorizationInvalidateResponse.empty());
		}

		try {
			AuthorizationInvalidateResponse response = executePipeline(request, command);
			invalidationIdempotencyPort.completeProcessedOutcome(eventId, request.kind(), response);
			log.info(
					"Authorization invalidate completed: eventId={}, kind={}, impacted={}, bumped={}, refreshed={}, evicted={}",
					eventId, request.kind(), response.impactedUserCount(), response.versionBumpedCount(),
					response.profileRefreshedCount(), response.profileEvictedCount());
			return response;
		}
		catch (RuntimeException ex) {
			invalidationIdempotencyPort.releaseClaim(eventId);
			throw ex;
		}
	}

	private AuthorizationInvalidateResponse executePipeline(AuthorizationInvalidateRequest request,
			InvalidationCommand command) {
		Set<Long> impactedUserIds = impactResolverRegistry.resolve(command.payload());
		int impactedUserCount = impactedUserIds.size();
		if (CollUtil.isEmpty(impactedUserIds)) {
			return AuthorizationInvalidateResponse.empty();
		}

		List<UserInvalidationState> invalidationStateList = userInvalidationStateQueryPort
			.loadByUserIds(impactedUserIds);
		UserInvalidationBuckets buckets = userInvalidationPolicy.partition(invalidationStateList);

		int batchSize = authorizationInvalidationProperties.getBatchSize();
		Set<Long> versionBumpUserIds = buckets.versionBumpUserIds();
		int versionBumpedCount = userPermVersionPort.incrementInBatches(versionBumpUserIds, batchSize);
		int profileRefreshedCount = authProfileMaterializationService.refreshInBatches(versionBumpUserIds, batchSize);

		Set<Long> evictOnlyUserIds = buckets.evictOnlyUserIds();
		int profileEvictedCount = authProfileMaterializationService.evictInBatches(evictOnlyUserIds, batchSize);
		kickSessionsForDeletedUsers(invalidationStateList);

		return new AuthorizationInvalidateResponse(impactedUserCount, versionBumpedCount, profileRefreshedCount,
				profileEvictedCount);
	}

	/**
	 * 逻辑删除用户：驱逐画像后踢掉全部会话（文档 §6 用户状态分桶）。
	 * @param states 用户状态快照
	 */
	private void kickSessionsForDeletedUsers(List<UserInvalidationState> states) {
		if (CollUtil.isEmpty(states)) {
			return;
		}

		Set<Long> deletedUserIds = new HashSet<>();
		for (UserInvalidationState state : states) {
			if (state != null && state.deleted() && state.userId() != null) {
				deletedUserIds.add(state.userId());
			}
		}
		if (CollUtil.isEmpty(deletedUserIds)) {
			return;
		}

		sessionAdminApplicationService.kickAllSessions(deletedUserIds);
		log.info("Kicked sessions for logically deleted users: count={}", deletedUserIds.size());
	}

}
