package com.auth.service.auth.authorization.application;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.authorization.application.invalidation.impact.ImpactResolverRegistry;
import com.auth.service.auth.authorization.application.invalidation.policy.UserInvalidationPolicy;
import com.auth.service.auth.authorization.config.AuthorizationInvalidationProperties;
import com.auth.service.auth.authorization.domain.invalidation.model.InvalidationCommand;
import com.auth.service.auth.authorization.domain.invalidation.model.InvalidationIdempotencyGate;
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
import java.util.Set;

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
	 * 执行授权失效流水线
	 * @param request 统一失效请求
	 * @return 执行结果摘要
	 */
	public AuthorizationInvalidateResponse invalidate(AuthorizationInvalidateRequest request) {
		// 构建命令
		InvalidationCommand command = new InvalidationCommand(request.eventId(), request.payload());
		String eventId = command.eventId();

		// 查询幂等门禁结果
		InvalidationIdempotencyGate gate = invalidationIdempotencyPort.acquireGate(eventId, request.kind());

		// 已处理完成，直接返回历史结果
		if (gate instanceof InvalidationIdempotencyGate.Completed completed) {
			log.debug("Authorization invalidate skipped (idempotent): eventId={}", eventId);
			return completed.response();
		}

		// 处理中，返回空结果
		if (gate instanceof InvalidationIdempotencyGate.InProgress) {
			log.warn("Authorization invalidate in progress by another worker: eventId={}", eventId);
			return AuthorizationInvalidateResponse.empty();
		}

		try {
			// 解析影响用户ID
			Set<Long> impactedUserIds = impactResolverRegistry.resolve(command.payload());

			// 无影响面时也需要更新数据库
			if (CollUtil.isEmpty(impactedUserIds)) {
				AuthorizationInvalidateResponse empty = AuthorizationInvalidateResponse.empty();
				invalidationIdempotencyPort.completeProcessedOutcome(eventId, request.kind(), empty);
				return empty;
			}

			// 查询用户状态快照
			List<UserInvalidationState> invalidationStateList = userInvalidationStateQueryPort
				.loadByUserIds(impactedUserIds);
			// 分区用户状态快照
			UserInvalidationBuckets buckets = userInvalidationPolicy.partition(invalidationStateList);
			Set<Long> evictOnlyUserIds = buckets.evictOnlyUserIds();
			Set<Long> versionBumpUserIds = buckets.versionBumpUserIds();

			// 获取批量大小
			int batchSize = authorizationInvalidationProperties.getBatchSize();

			// 分批递增版本
			int versionBumpedCount = userPermVersionPort.incrementInBatches(versionBumpUserIds, batchSize);
			int profileRefreshedCount = authProfileMaterializationService.refreshInBatches(versionBumpUserIds,
					batchSize);

			// 分批驱逐画像
			int profileEvictedCount = authProfileMaterializationService.evictInBatches(evictOnlyUserIds, batchSize);

			// 逻辑删除用户：驱逐画像后踢掉全部会话
			kickSessionsForDeletedUsers(invalidationStateList);

			// 构建执行结果
			int impactedUserCount = impactedUserIds.size();
			AuthorizationInvalidateResponse response = new AuthorizationInvalidateResponse(impactedUserCount,
					versionBumpedCount, profileRefreshedCount, profileEvictedCount);
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
	 * 逻辑删除用户
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
