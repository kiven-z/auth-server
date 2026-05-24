package com.auth.service.auth.authorization.application;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.authorization.application.invalidation.impact.ImpactResolverRegistry;
import com.auth.service.auth.authorization.application.invalidation.policy.UserInvalidationPolicy;
import com.auth.service.auth.authorization.config.AuthorizationInvalidationProperties;
import com.auth.service.auth.authorization.domain.invalidation.model.InvalidationCommand;
import com.auth.service.auth.authorization.domain.invalidation.model.UserInvalidationBuckets;
import com.auth.service.auth.authorization.domain.invalidation.model.UserInvalidationState;
import com.auth.service.auth.authorization.domain.invalidation.port.InvalidationIdempotencyPort;
import com.auth.service.auth.authorization.domain.invalidation.port.UserInvalidationStateQueryPort;
import com.auth.service.auth.authorization.domain.invalidation.port.UserPermVersionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

	private final AuthorizationInvalidationProperties authorizationInvalidationProperties;

	/**
	 * 执行授权失效流水线。
	 * @param request 统一失效请求
	 * @return 执行结果摘要
	 */
	public AuthorizationInvalidateResponse invalidate(AuthorizationInvalidateRequest request) {
		// 创建失效命令
		InvalidationCommand command = new InvalidationCommand(request.eventId(), request.payload());

		// 获取事件ID
		String eventId = command.eventId();

		// 查询缓存
		Optional<AuthorizationInvalidateResponse> cached = invalidationIdempotencyPort.findProcessedOutcome(eventId);
		if (cached.isPresent()) {
			log.debug("Authorization invalidate skipped (idempotent): eventId={}", eventId);
			return cached.get();
		}

		// 解析影响用户ID
		Set<Long> impactedUserIds = impactResolverRegistry.resolve(command.payload());
		int impactedUserCount = impactedUserIds.size();
		if (CollUtil.isEmpty(impactedUserIds)) {
			AuthorizationInvalidateResponse empty = AuthorizationInvalidateResponse.empty();
			invalidationIdempotencyPort.saveProcessedOutcome(eventId, request.kind(), empty);
			return empty;
		}

		// 分桶
		List<UserInvalidationState> invalidationStateList = userInvalidationStateQueryPort
			.loadByUserIds(impactedUserIds);
		UserInvalidationBuckets buckets = userInvalidationPolicy.partition(invalidationStateList);

		// 递增版本
		int batchSize = authorizationInvalidationProperties.getBatchSize();
		Set<Long> userIds = buckets.versionBumpUserIds();
		int versionBumpedCount = userPermVersionPort.incrementInBatches(userIds, batchSize);

		// 刷新画像
		int profileRefreshedCount = authProfileMaterializationService.refreshInBatches(userIds, batchSize);

		// 驱逐画像
		Set<Long> evictOnlyUserIds = buckets.evictOnlyUserIds();
		int profileEvictedCount = authProfileMaterializationService.evictInBatches(evictOnlyUserIds, batchSize);

		// 创建响应
		AuthorizationInvalidateResponse response = new AuthorizationInvalidateResponse(impactedUserCount,
				versionBumpedCount, profileRefreshedCount, profileEvictedCount);
		invalidationIdempotencyPort.saveProcessedOutcome(eventId, request.kind(), response);
		return response;
	}

}
