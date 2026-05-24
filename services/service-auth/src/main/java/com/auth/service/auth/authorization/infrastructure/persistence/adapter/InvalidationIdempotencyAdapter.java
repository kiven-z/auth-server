package com.auth.service.auth.authorization.infrastructure.persistence.adapter;

import cn.hutool.core.util.ObjectUtil;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.authorization.domain.invalidation.port.InvalidationIdempotencyPort;
import com.auth.service.auth.authorization.infrastructure.persistence.AuthorizationInvalidationEventMapper;
import com.auth.service.auth.authorization.infrastructure.persistence.projection.AuthorizationInvalidationEventProjection;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
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
	 * 转换为授权失效响应
	 * @param projection 授权失效事件投影
	 * @return 授权失效响应
	 */
	private static AuthorizationInvalidateResponse toResponse(AuthorizationInvalidationEventProjection projection) {
		return new AuthorizationInvalidateResponse(ObjectUtil.defaultIfNull(projection.getImpactedUserCount(), 0),
				ObjectUtil.defaultIfNull(projection.getVersionBumpedCount(), 0),
				ObjectUtil.defaultIfNull(projection.getProfileRefreshedCount(), 0),
				ObjectUtil.defaultIfNull(projection.getProfileEvictedCount(), 0));
	}

	@Override
	public Optional<AuthorizationInvalidateResponse> findProcessedOutcome(String eventId) {
		AuthorizationInvalidationEventProjection projection = authorizationInvalidationEventMapper
			.selectByEventId(eventId);
		if (projection == null) {
			return Optional.empty();
		}

		AuthorizationInvalidateResponse response = toResponse(projection);
		return Optional.of(response);
	}

	@Override
	public void saveProcessedOutcome(String eventId, AuthorizationChangeKind kind,
			AuthorizationInvalidateResponse response) {
		AuthorizationInvalidationEventProjection record = new AuthorizationInvalidationEventProjection();
		record.setId(IdWorker.getId());
		record.setEventId(eventId);
		record.setChangeKind(kind.name());
		record.setImpactedUserCount(response.impactedUserCount());
		record.setVersionBumpedCount(response.versionBumpedCount());
		record.setProfileRefreshedCount(response.profileRefreshedCount());
		record.setProfileEvictedCount(response.profileEvictedCount());
		record.setProcessedAt(LocalDateTime.now());
		authorizationInvalidationEventMapper.insertProcessedEvent(record);
	}

}
