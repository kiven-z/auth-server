package com.auth.service.auth.infrastructure.persistence.adapter;

import com.auth.service.auth.application.port.invalidation.InvalidationEventOpsPort;
import com.auth.service.auth.infrastructure.persistence.AuthorizationInvalidationEventMapper;
import com.auth.service.auth.infrastructure.persistence.projection.AuthorizationInvalidationEventProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * {@link InvalidationEventOpsPort} 适配器
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Repository
public class InvalidationEventOpsAdapter implements InvalidationEventOpsPort {

	private final AuthorizationInvalidationEventMapper authorizationInvalidationEventMapper;

	@Override
	public Integer findImpactedUserCountByEventId(String eventId) {
		AuthorizationInvalidationEventProjection projection = authorizationInvalidationEventMapper
			.selectByEventId(eventId);
		return projection != null ? projection.getImpactedUserCount() : null;
	}

	@Override
	public int deleteStaleProcessingClaims(LocalDateTime cutoffTime, int batchSize) {
		return authorizationInvalidationEventMapper.deleteStaleProcessingClaims(cutoffTime, batchSize);
	}

	@Override
	public int deleteCompletedBefore(LocalDateTime cutoffTime, int batchSize) {
		return authorizationInvalidationEventMapper.deleteCompletedBefore(cutoffTime, batchSize);
	}

}
