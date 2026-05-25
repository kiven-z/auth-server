package com.auth.service.auth.authorization.application;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

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

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 授权失效编排唯一入口
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
		// 构建失效命令
		InvalidationCommand command = new InvalidationCommand(request.eventId(), request.payload());
		String eventId = command.eventId();

		// 查询幂等结果
		Optional<AuthorizationInvalidateResponse> cached = invalidationIdempotencyPort.findProcessedOutcome(eventId);
		if (cached.isPresent()) {
			log.debug("Authorization invalidate skipped (idempotent): eventId={}", eventId);
			return cached.get();
		}

		// 抢占幂等锁
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
			// 执行失效流水线
			AuthorizationInvalidateResponse response = executePipeline(command);
			invalidationIdempotencyPort.completeProcessedOutcome(eventId, request.kind(), response);
			log.info(
					"Authorization invalidate completed: eventId={}, kind={}, impacted={}, bumped={}, refreshed={}, evicted={}",
					eventId, request.kind(), response.impactedUserCount(), response.versionBumpedCount(),
					response.profileRefreshedCount(), response.profileEvictedCount());
			return response;
		}
		catch (RuntimeException ex) {
			// 释放幂等锁
			invalidationIdempotencyPort.releaseClaim(eventId);
			throw ex;
		}
	}

	/**
	 * 执行失效流水线
	 * @param command 失效命令
	 * @return 执行结果摘要
	 */
	private AuthorizationInvalidateResponse executePipeline(InvalidationCommand command) {
		// 解析影响用户ID
		Set<Long> impactedUserIds = impactResolverRegistry.resolve(command.payload());
		int impactedUserCount = impactedUserIds.size();
		if (CollUtil.isEmpty(impactedUserIds)) {
			return AuthorizationInvalidateResponse.empty();
		}

		// 查询用户状态快照
		List<UserInvalidationState> invalidationStateList = userInvalidationStateQueryPort
			.loadByUserIds(impactedUserIds);
		// 分区用户状态快照
		UserInvalidationBuckets buckets = userInvalidationPolicy.partition(invalidationStateList);

		// 获取批量大小
		int batchSize = authorizationInvalidationProperties.getBatchSize();
		Set<Long> versionBumpUserIds = buckets.versionBumpUserIds();

		// 分批递增版本
		int versionBumpedCount = userPermVersionPort.incrementInBatches(versionBumpUserIds, batchSize);
		int profileRefreshedCount = authProfileMaterializationService.refreshInBatches(versionBumpUserIds, batchSize);

		// 分批驱逐画像
		Set<Long> evictOnlyUserIds = buckets.evictOnlyUserIds();
		int profileEvictedCount = authProfileMaterializationService.evictInBatches(evictOnlyUserIds, batchSize);

		// 逻辑删除用户：驱逐画像后踢掉全部会话
		kickSessionsForDeletedUsers(invalidationStateList);

		// 构建执行结果摘要
		return new AuthorizationInvalidateResponse(impactedUserCount, versionBumpedCount, profileRefreshedCount,
				profileEvictedCount);
	}

	/**
	 * 逻辑删除用户：驱逐画像后踢掉全部会话（文档 §6 用户状态分桶）。
	 * @param states 用户状态快照
	 */
	private void kickSessionsForDeletedUsers(List<UserInvalidationState> states) {
		// 如果用户状态快照为空，则返回
		if (CollUtil.isEmpty(states)) {
			return;
		}

		// 获取逻辑删除用户ID集合
		Set<Long> deletedUserIds = new HashSet<>();
		for (UserInvalidationState state : states) {
			if (state != null && state.deleted() && state.userId() != null) {
				deletedUserIds.add(state.userId());
			}
		}
		// 如果逻辑删除用户ID集合为空，则返回
		if (CollUtil.isEmpty(deletedUserIds)) {
			return;
		}

		// 踢掉全部会话
		sessionAdminApplicationService.kickAllSessions(deletedUserIds);
		log.info("Kicked sessions for logically deleted users: count={}", deletedUserIds.size());
	}

}
