package com.auth.service.system.common.feign.fallback;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.system.common.feign.AuthInnerFeignClient;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventDetailInnerDTO;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventInnerQuery;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventPageInnerDTO;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventStatsInnerDTO;
import com.auth.service.system.common.feign.dto.EffectiveCodesInnerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fallback
 *
 * @author Bunny
 */
@Slf4j
@Component
public class AuthInnerFeignClientFallback implements AuthInnerFeignClient {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<String> refreshUserCache(List<Long> userIds) {
		log.error("AuthenticationInnerFeignClientFallback-刷新用户缓存 失败，用户IDs：{}", userIds);

		return Result.error("刷新缓存服务暂不可用");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<EffectiveCodesInnerDTO> getEffectiveCodes(Long userId) {
		log.error("AuthInnerPrincipalFeignClientFallback: getEffectiveCodes failed, userId={}", userId);
		return Result.success(new EffectiveCodesInnerDTO());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<AuthorizationInvalidateResponse> invalidate(AuthorizationInvalidateRequest request) {
		String eventId = request != null ? request.eventId() : null;
		log.error("AuthInnerFeignClientFallback: invalidate failed, eventId={}", eventId);
		return Result.error("Authorization invalidation service is unavailable");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<PageResponse<AuthorizationInvalidationEventPageInnerDTO>> getInvalidationEventPage(
			AuthorizationInvalidationEventInnerQuery query) {
		log.error("AuthInnerFeignClientFallback: getInvalidationEventPage failed");
		return Result.error("Authorization invalidation event query is unavailable");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<AuthorizationInvalidationEventDetailInnerDTO> getInvalidationEventDetail(Long id) {
		log.error("AuthInnerFeignClientFallback: getInvalidationEventDetail failed, id={}", id);
		return Result.error("Authorization invalidation event query is unavailable");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<AuthorizationInvalidationEventStatsInnerDTO> getInvalidationEventSummary() {
		log.error("AuthInnerFeignClientFallback: getInvalidationEventSummary failed");
		return Result.error("Authorization invalidation event summary is unavailable");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<Boolean> releaseInvalidationEventClaim(String eventId) {
		log.error("AuthInnerFeignClientFallback: releaseInvalidationEventClaim failed, eventId={}", eventId);
		return Result.error("Authorization invalidation event release claim is unavailable");
	}

}
