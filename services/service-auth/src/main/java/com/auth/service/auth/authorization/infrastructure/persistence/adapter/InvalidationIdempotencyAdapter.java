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
 * 授权失效幂等 Port 适配器。
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
	 * 转换为授权失效响应（跳过处理中占位行）。
	 * @param projection 授权失效事件投影
	 * @return 授权失效响应；处理中占位时为空
	 */
	private static Optional<AuthorizationInvalidateResponse> toResponseIfCompleted(
			AuthorizationInvalidationEventProjection projection) {
		if (projection == null || InvalidationProcessingMarker.isProcessing(projection.getImpactedUserCount())) {
			return Optional.empty();
		}

		AuthorizationInvalidateResponse response = new AuthorizationInvalidateResponse(
				ObjectUtil.defaultIfNull(projection.getImpactedUserCount(), 0),
				ObjectUtil.defaultIfNull(projection.getVersionBumpedCount(), 0),
				ObjectUtil.defaultIfNull(projection.getProfileRefreshedCount(), 0),
				ObjectUtil.defaultIfNull(projection.getProfileEvictedCount(), 0));
		return Optional.of(response);
	}

	@Override
	public Optional<AuthorizationInvalidateResponse> findProcessedOutcome(String eventId) {
		AuthorizationInvalidationEventProjection projection = authorizationInvalidationEventMapper
			.selectByEventId(eventId);
		return toResponseIfCompleted(projection);
	}

	@Override
	public IdempotencyClaimStatus tryClaim(String eventId, AuthorizationChangeKind kind) {
		Optional<AuthorizationInvalidateResponse> completed = findProcessedOutcome(eventId);
		if (completed.isPresent()) {
			return IdempotencyClaimStatus.ALREADY_COMPLETED;
		}

		AuthorizationInvalidationEventProjection claim = buildProcessingClaim(eventId, kind);
		try {
			authorizationInvalidationEventMapper.insertProcessingClaim(claim);
			return IdempotencyClaimStatus.CLAIMED;
		}
		catch (DuplicateKeyException ex) {
			AuthorizationInvalidationEventProjection existing = authorizationInvalidationEventMapper
				.selectByEventId(eventId);
			if (existing == null) {
				return IdempotencyClaimStatus.IN_PROGRESS;
			}
			if (InvalidationProcessingMarker.isProcessing(existing.getImpactedUserCount())) {
				return IdempotencyClaimStatus.IN_PROGRESS;
			}
			return IdempotencyClaimStatus.ALREADY_COMPLETED;
		}
	}

	@Override
	public void completeProcessedOutcome(String eventId, AuthorizationChangeKind kind,
			AuthorizationInvalidateResponse response) {
		AuthorizationInvalidationEventProjection record = new AuthorizationInvalidationEventProjection();
		record.setEventId(eventId);
		record.setChangeKind(kind.name());
		record.setImpactedUserCount(response.impactedUserCount());
		record.setVersionBumpedCount(response.versionBumpedCount());
		record.setProfileRefreshedCount(response.profileRefreshedCount());
		record.setProfileEvictedCount(response.profileEvictedCount());
		record.setProcessedAt(LocalDateTime.now());

		int updated = authorizationInvalidationEventMapper.updateProcessedOutcome(record);
		if (updated == 0) {
			throw new IllegalStateException(
					"Failed to complete invalidation idempotency record, eventId=" + eventId + " (claim missing?)");
		}
	}

	@Override
	public void releaseClaim(String eventId) {
		authorizationInvalidationEventMapper.deleteProcessingClaim(eventId);
	}

	private static AuthorizationInvalidationEventProjection buildProcessingClaim(String eventId,
			AuthorizationChangeKind kind) {
		LocalDateTime now = LocalDateTime.now();
		AuthorizationInvalidationEventProjection record = new AuthorizationInvalidationEventProjection();
		record.setId(IdWorker.getId());
		record.setEventId(eventId);
		record.setChangeKind(kind.name());
		record.setImpactedUserCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		record.setVersionBumpedCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		record.setProfileRefreshedCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		record.setProfileEvictedCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		record.setProcessedAt(now);
		return record;
	}

}
