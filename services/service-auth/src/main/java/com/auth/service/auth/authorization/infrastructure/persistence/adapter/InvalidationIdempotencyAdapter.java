package com.auth.service.auth.authorization.infrastructure.persistence.adapter;

import cn.hutool.core.util.ObjectUtil;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.authorization.domain.invalidation.model.IdempotencyClaimStatus;
import com.auth.service.auth.authorization.domain.invalidation.model.InvalidationProcessingMarker;
import com.auth.service.auth.authorization.domain.invalidation.port.InvalidationIdempotencyPort;
import com.auth.service.auth.authorization.infrastructure.persistence.AuthorizationInvalidationEventMapper;
import com.auth.service.auth.authorization.infrastructure.persistence.projection.AuthorizationInvalidationEventProjection;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

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
		projection.setImpactedUserCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		projection.setVersionBumpedCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		projection.setProfileRefreshedCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		projection.setProfileEvictedCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		projection.setProcessedAt(now);
		return projection;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<AuthorizationInvalidateResponse> findProcessedOutcome(String eventId) {
		// 查询幂等结果
		AuthorizationInvalidationEventProjection projection = authorizationInvalidationEventMapper
			.selectByEventId(eventId);

		if (projection == null || InvalidationProcessingMarker.isProcessing(projection.getImpactedUserCount())) {
			return Optional.empty();
		}

		// 转换为授权失效响应，为Null时返回0
		AuthorizationInvalidateResponse response = new AuthorizationInvalidateResponse(
				ObjectUtil.defaultIfNull(projection.getImpactedUserCount(), 0),
				ObjectUtil.defaultIfNull(projection.getVersionBumpedCount(), 0),
				ObjectUtil.defaultIfNull(projection.getProfileRefreshedCount(), 0),
				ObjectUtil.defaultIfNull(projection.getProfileEvictedCount(), 0));
		return Optional.of(response);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IdempotencyClaimStatus tryClaim(String eventId, AuthorizationChangeKind kind) {
		// 查询幂等结果
		Optional<AuthorizationInvalidateResponse> completed = findProcessedOutcome(eventId);
		if (completed.isPresent()) {
			return IdempotencyClaimStatus.ALREADY_COMPLETED;
		}

		// 构建处理中占位记录
		AuthorizationInvalidationEventProjection claim = buildProcessingClaim(eventId, kind);
		try {
			// 插入处理中占位记录
			authorizationInvalidationEventMapper.insertProcessingClaim(claim);
			return IdempotencyClaimStatus.CLAIMED;
		}
		catch (DuplicateKeyException ex) {
			// 查询处理中占位记录
			AuthorizationInvalidationEventProjection existing = authorizationInvalidationEventMapper
				.selectByEventId(eventId);
			if (existing == null) {
				// 如果处理中占位记录不存在，则返回处理中
				return IdempotencyClaimStatus.IN_PROGRESS;
			}
			if (InvalidationProcessingMarker.isProcessing(existing.getImpactedUserCount())) {
				// 如果处理中占位记录存在，则返回处理中
				return IdempotencyClaimStatus.IN_PROGRESS;
			}
			// 如果处理中占位记录存在，则返回已完成
			return IdempotencyClaimStatus.ALREADY_COMPLETED;
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
	 */
	@Override
	public void releaseClaim(String eventId) {
		authorizationInvalidationEventMapper.deleteProcessingClaim(eventId);
	}

}
