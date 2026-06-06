package com.auth.service.auth.authorization.application.ops;

import com.auth.service.auth.authorization.domain.invalidation.model.InvalidationProcessingMarker;
import com.auth.service.auth.authorization.domain.invalidation.port.InvalidationIdempotencyPort;
import com.auth.service.auth.authorization.infrastructure.persistence.AuthorizationInvalidationEventMapper;
import com.auth.service.auth.authorization.infrastructure.persistence.projection.AuthorizationInvalidationEventProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	 * 释放 processing 占位行，允许 Outbox 重新投递。
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

}
