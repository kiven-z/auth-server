package com.auth.service.auth.infrastructure.persistence.adapter;

import cn.hutool.core.util.ObjectUtil;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.application.port.invalidation.InvalidationIdempotencyPort;
import com.auth.service.auth.domain.authorization.invalidation.model.InvalidationIdempotencyGate;
import com.auth.service.auth.domain.authorization.invalidation.model.InvalidationProcessingMarker;
import com.auth.service.auth.infrastructure.persistence.AuthorizationInvalidationEventMapper;
import com.auth.service.auth.infrastructure.persistence.projection.AuthorizationInvalidationEventProjection;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 授权失效幂等 Port 适配器
 *
 * @author Bunny
 */
@Repository
public class InvalidationIdempotencyAdapter implements InvalidationIdempotencyPort {

	private final AuthorizationInvalidationEventMapper authorizationInvalidationEventMapper;

	public InvalidationIdempotencyAdapter(AuthorizationInvalidationEventMapper authorizationInvalidationEventMapper) {
		this.authorizationInvalidationEventMapper = authorizationInvalidationEventMapper;
	}

	/**
	 * 构建处理中占位记录
	 * @param eventId 业务事件 ID
	 * @param kind 变更维度
	 * @return 处理中占位记录
	 */
	private static AuthorizationInvalidationEventProjection buildProcessingClaim(String eventId,
			AuthorizationChangeKind kind) {
		LocalDateTime now = LocalDateTime.now();
		AuthorizationInvalidationEventProjection projection = new AuthorizationInvalidationEventProjection();
		projection.setId(IdWorker.getId());
		projection.setEventId(eventId);
		projection.setChangeKind(kind.name());
		// 全部设置为-1
		projection.setImpactedUserCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		projection.setVersionBumpedCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		projection.setProfileRefreshedCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		projection.setProfileEvictedCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		projection.setProcessedAt(now);
		return projection;
	}

	/**
	 * 将持久化投影转换为授权失效响应。
	 * @param projection 事件投影
	 * @return 执行结果摘要
	 */
	private static AuthorizationInvalidateResponse toResponse(AuthorizationInvalidationEventProjection projection) {
		return new AuthorizationInvalidateResponse(ObjectUtil.defaultIfNull(projection.getImpactedUserCount(), 0),
				ObjectUtil.defaultIfNull(projection.getVersionBumpedCount(), 0),
				ObjectUtil.defaultIfNull(projection.getProfileRefreshedCount(), 0),
				ObjectUtil.defaultIfNull(projection.getProfileEvictedCount(), 0));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InvalidationIdempotencyGate acquireGate(String eventId, AuthorizationChangeKind kind) {
		// 查询已处理记录
		AuthorizationInvalidationEventProjection projection = authorizationInvalidationEventMapper
			.selectByEventId(eventId);

		// 已处理完成，直接返回历史结果
		if (projection != null && !InvalidationProcessingMarker.isProcessing(projection.getImpactedUserCount())) {
			AuthorizationInvalidateResponse response = toResponse(projection);
			return new InvalidationIdempotencyGate.Completed(response);
		}

		try {
			// 处理中，插入占位记录
			AuthorizationInvalidationEventProjection claim = buildProcessingClaim(eventId, kind);
			authorizationInvalidationEventMapper.insertProcessingClaim(claim);
			return new InvalidationIdempotencyGate.Claimed();
		}
		catch (DuplicateKeyException ex) {
			AuthorizationInvalidationEventProjection existing = authorizationInvalidationEventMapper
				.selectByEventId(eventId);
			// 占位记录仍为处理中，返回处理中结果
			if (existing == null || InvalidationProcessingMarker.isProcessing(existing.getImpactedUserCount())) {
				return new InvalidationIdempotencyGate.InProgress();
			}

			AuthorizationInvalidateResponse response = toResponse(existing);
			return new InvalidationIdempotencyGate.Completed(response);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void completeProcessedOutcome(String eventId, AuthorizationChangeKind kind,
			AuthorizationInvalidateResponse response) {
		AuthorizationInvalidationEventProjection projection = new AuthorizationInvalidationEventProjection();
		projection.setEventId(eventId);
		projection.setChangeKind(kind.name());
		projection.setImpactedUserCount(response.impactedUserCount());
		projection.setVersionBumpedCount(response.versionBumpedCount());
		projection.setProfileRefreshedCount(response.profileRefreshedCount());
		projection.setProfileEvictedCount(response.profileEvictedCount());
		projection.setProcessedAt(LocalDateTime.now());

		int updated = authorizationInvalidationEventMapper.updateProcessedOutcome(projection);
		if (updated == 0) {
			throw new IllegalStateException(
					"Failed to complete invalidation idempotency record, eventId=" + eventId + " (claim missing?)");
		}
	}

	/**
	 * {@inheritDoc}
	 * @return
	 */
	@Override
	public boolean releaseClaim(String eventId) {
		return authorizationInvalidationEventMapper.deleteProcessingClaim(eventId) > 0;
	}

}
