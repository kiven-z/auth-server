package com.auth.service.auth.application.authorization.command;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.application.authorization.invalidation.impact.ImpactResolverRegistry;
import com.auth.service.auth.application.authorization.invalidation.policy.UserInvalidationPolicy;
import com.auth.service.auth.application.port.invalidation.AuthorizationInvalidationEventPort;
import com.auth.service.auth.application.port.session.SessionRevocationPort;
import com.auth.service.auth.application.port.invalidation.UserInvalidationStatePort;
import com.auth.service.auth.config.AuthorizationInvalidationProperties;
import com.auth.service.auth.domain.authorization.model.InvalidationCommand;
import com.auth.service.auth.domain.authorization.model.InvalidationIdempotencyGate;
import com.auth.service.auth.domain.authorization.model.UserInvalidationBuckets;
import com.auth.service.auth.domain.authorization.model.UserInvalidationState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 授权失效编排唯一入口
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AuthorizationInvalidationService {

	private final AuthorizationInvalidationEventPort authorizationInvalidationEventPort;

	private final ImpactResolverRegistry impactResolverRegistry;

	private final UserInvalidationStatePort userInvalidationStatePort;

	private final UserInvalidationPolicy userInvalidationPolicy;

	private final AuthProfileMaterializationService authProfileMaterializationService;

	private final SessionRevocationPort sessionRevocationPort;

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
		InvalidationIdempotencyGate gate = authorizationInvalidationEventPort.acquireGate(eventId, request.kind());

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
				authorizationInvalidationEventPort.completeProcessedOutcome(eventId, request.kind(), empty);
				return empty;
			}

			// 查询用户状态快照
			List<UserInvalidationState> invalidationStateList = userInvalidationStatePort
				.loadByUserIds(impactedUserIds);
			// 分区用户状态快照
			UserInvalidationBuckets buckets = userInvalidationPolicy.partition(invalidationStateList);
			Set<Long> evictOnlyUserIds = buckets.evictOnlyUserIds();
			Set<Long> versionBumpUserIds = buckets.versionBumpUserIds();

			// 获取批量大小
			int batchSize = authorizationInvalidationProperties.getBatchSize();

			// 分批递增版本
			int versionBumpedCount = userInvalidationStatePort.incrementPermVersionInBatches(versionBumpUserIds,
					batchSize);
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
			authorizationInvalidationEventPort.completeProcessedOutcome(eventId, request.kind(), response);
			log.info(
					"Authorization invalidate completed: eventId={}, kind={}, impacted={}, bumped={}, refreshed={}, evicted={}",
					eventId, request.kind(), response.impactedUserCount(), response.versionBumpedCount(),
					response.profileRefreshedCount(), response.profileEvictedCount());
			return response;
		}
		catch (RuntimeException ex) {
			// 释放幂等锁
			authorizationInvalidationEventPort.releaseClaim(eventId);
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
		Set<Long> deletedUserIds = states.stream()
			.filter(state -> state != null && state.deleted() && state.userId() != null)
			.map(UserInvalidationState::userId)
			.collect(Collectors.toUnmodifiableSet());

		// 如果逻辑删除用户ID集合为空，则返回
		if (CollUtil.isEmpty(deletedUserIds)) {
			return;
		}

		// 踢掉全部会话
		sessionRevocationPort.kickAllSessions(deletedUserIds);
		log.info("Kicked sessions for logically deleted users: count={}", deletedUserIds.size());
	}

}
