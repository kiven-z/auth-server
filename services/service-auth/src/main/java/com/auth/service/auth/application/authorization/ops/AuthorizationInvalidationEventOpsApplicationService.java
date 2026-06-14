package com.auth.service.auth.application.authorization.ops;

import com.auth.service.auth.application.port.invalidation.InvalidationIdempotencyPort;
import com.auth.service.auth.domain.authorization.invalidation.model.InvalidationProcessingMarker;
import com.auth.service.auth.infrastructure.persistence.AuthorizationInvalidationEventMapper;
import com.auth.service.auth.infrastructure.persistence.projection.AuthorizationInvalidationEventProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 授权失效幂等事件运维操作
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class AuthorizationInvalidationEventOpsApplicationService {

	private final AuthorizationInvalidationEventMapper authorizationInvalidationEventMapper;

	private final InvalidationIdempotencyPort invalidationIdempotencyPort;

	/**
	 * 释放 processing 占位行，允许 Outbox 重新投递
	 * @param eventId 业务事件 ID
	 * @return 是否实际删除了占位行
	 */
	public boolean releaseProcessingClaim(String eventId) {
		AuthorizationInvalidationEventProjection projection = authorizationInvalidationEventMapper
			.selectByEventId(eventId);

		if (projection == null || !InvalidationProcessingMarker.isProcessing(projection.getImpactedUserCount())) {
			return false;
		}

		return invalidationIdempotencyPort.releaseClaim(eventId);
	}

	/**
	 * 批量释放超时的 processing 占位行
	 * @param cutoffTime 占位 update_time 早于此时间视为超时
	 * @param batchSize 本批删除上限
	 * @return 实际删除行数
	 */
	public int cleanupStaleProcessingClaims(LocalDateTime cutoffTime, int batchSize) {
		return authorizationInvalidationEventMapper.deleteStaleProcessingClaims(cutoffTime, batchSize);
	}

	/**
	 * 批量删除已完成的过期幂等事件
	 * @param cutoffTime processed_at 早于此时间的已完成行可被删除
	 * @param batchSize 本批删除上限
	 * @return 实际删除行数
	 */
	public int purgeCompletedBefore(LocalDateTime cutoffTime, int batchSize) {
		return authorizationInvalidationEventMapper.deleteCompletedBefore(cutoffTime, batchSize);
	}

}
