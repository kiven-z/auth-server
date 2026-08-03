package com.auth.service.system.authorization.feign.fallback;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.system.authorization.feign.AuthorizationInnerFeignClient;
import com.auth.service.system.authorization.feign.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 授权域内部 Feign 降级
 *
 * @author Bunny
 */
@Slf4j
@Component
public class AuthorizationInnerFeignClientFallback implements AuthorizationInnerFeignClient {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<EffectiveCodesInnerDTO> getEffectiveCodes(Long userId) {
		log.error("AuthorizationInnerFeignClientFallback: getEffectiveCodes failed, userId={}", userId);
		return Result.success(new EffectiveCodesInnerDTO());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<Void> refreshProfiles(List<Long> userIds) {
		int size = userIds == null ? 0 : userIds.size();
		log.error("AuthorizationInnerFeignClientFallback: refreshProfiles failed, size={}", size);
		return Result.error("Authorization profile refresh is unavailable");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<AuthorizationInvalidateResponse> invalidate(AuthorizationInvalidateRequest request) {
		String eventId = request != null ? request.eventId() : null;
		log.error("AuthorizationInnerFeignClientFallback: invalidate failed, eventId={}", eventId);
		return Result.error("Authorization invalidation service is unavailable");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<PageResponse<AuthorizationInvalidationEventPageInnerDTO>> getInvalidationEventPage(
			AuthorizationInvalidationEventInnerQuery query) {
		log.error("AuthorizationInnerFeignClientFallback: getInvalidationEventPage failed");
		return Result.error("Authorization invalidation event query is unavailable");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<AuthorizationInvalidationEventDetailInnerDTO> getInvalidationEventDetail(Long id) {
		log.error("AuthorizationInnerFeignClientFallback: getInvalidationEventDetail failed, id={}", id);
		return Result.error("Authorization invalidation event query is unavailable");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<AuthorizationInvalidationEventStatsInnerDTO> getInvalidationEventSummary() {
		log.error("AuthorizationInnerFeignClientFallback: getInvalidationEventSummary failed");
		return Result.error("Authorization invalidation event summary is unavailable");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<Boolean> releaseInvalidationEventClaim(String eventId) {
		log.error("AuthorizationInnerFeignClientFallback: releaseInvalidationEventClaim failed, eventId={}", eventId);
		return Result.error("Authorization invalidation event release claim is unavailable");
	}

}
