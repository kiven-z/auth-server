package com.auth.service.auth.infrastructure.persistence.adapter;

import java.time.LocalDateTime;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.application.port.invalidation.AuthorizationInvalidationEventPort;
import com.auth.service.auth.convert.AuthorizationInvalidationEventMappingSupport;
import com.auth.service.auth.domain.authorization.model.InvalidationIdempotencyGate;
import com.auth.service.auth.domain.authorization.model.InvalidationProcessingMarker;
import com.auth.service.auth.infrastructure.persistence.mapper.AuthorizationInvalidationEventMapper;
import com.auth.service.auth.infrastructure.persistence.projection.AuthorizationInvalidationEventProjection;

import lombok.RequiredArgsConstructor;

/**
 * 授权失效幂等事件适配器。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Repository
public class AuthorizationInvalidationEventAdapter implements AuthorizationInvalidationEventPort {

	private final AuthorizationInvalidationEventMapper invalidationEventMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InvalidationIdempotencyGate acquireGate(String eventId, AuthorizationChangeKind kind) {
		// 查询幂等事件
		AuthorizationInvalidationEventProjection projection = invalidationEventMapper.selectByEventId(eventId);

		// 如果幂等事件不为空且不是处理中，则返回已完成
		if (projection != null && !InvalidationProcessingMarker.isProcessing(projection.getImpactedUserCount())) {
			return new InvalidationIdempotencyGate.Completed(
					AuthorizationInvalidationEventMappingSupport.toResponse(projection));
		}

		try {
			// 插入处理中占位行
			invalidationEventMapper.insertProcessingClaim(
					AuthorizationInvalidationEventMappingSupport.buildProcessingClaim(eventId, kind));

			return new InvalidationIdempotencyGate.Claimed();
		}
		catch (DuplicateKeyException ex) {
			// 查询幂等事件
			AuthorizationInvalidationEventProjection existing = invalidationEventMapper.selectByEventId(eventId);

			// 如果幂等事件为空或处理中，则返回处理中
			if (existing == null || InvalidationProcessingMarker.isProcessing(existing.getImpactedUserCount())) {
				return new InvalidationIdempotencyGate.InProgress();
			}

			// 如果幂等事件不为空且处理中，则返回已完成
			AuthorizationInvalidateResponse response = AuthorizationInvalidationEventMappingSupport
				.toResponse(existing);
			return new InvalidationIdempotencyGate.Completed(response);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void completeProcessedOutcome(String eventId, AuthorizationChangeKind kind,
			AuthorizationInvalidateResponse response) {
		// 构建完成态投影
		AuthorizationInvalidationEventProjection projection = AuthorizationInvalidationEventMappingSupport
			.toProcessedOutcomeProjection(eventId, kind, response);

		// 更新幂等事件
		int updated = invalidationEventMapper.updateProcessedOutcome(projection);
		if (updated == 0) {
			throw new IllegalStateException(
					"Failed to complete invalidation idempotency record, eventId=" + eventId + " (claim missing?)");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean releaseClaim(String eventId) {
		return invalidationEventMapper.deleteProcessingClaim(eventId) > 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer findImpactedUserCountByEventId(String eventId) {
		AuthorizationInvalidationEventProjection projection = invalidationEventMapper.selectByEventId(eventId);
		return projection != null ? projection.getImpactedUserCount() : null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int deleteStaleProcessingClaims(LocalDateTime cutoffTime, int batchSize) {
		return invalidationEventMapper.deleteStaleProcessingClaims(cutoffTime, batchSize);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int deleteCompletedBefore(LocalDateTime cutoffTime, int batchSize) {
		return invalidationEventMapper.deleteCompletedBefore(cutoffTime, batchSize);
	}

}
