package com.auth.service.system.common.feign.fallback;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.system.common.feign.AuthInnerFeignClient;
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
	 * 刷新用户缓存
	 * @param userIds 用户ID列表
	 * @return 刷新结果
	 */
	@Override
	public Result<String> refreshUserCache(List<Long> userIds) {
		log.error("AuthenticationInnerFeignClientFallback-刷新用户缓存 失败，用户IDs：{}", userIds);

		// 方案A：返回失败结果，让调用方处理
		return Result.error("刷新缓存服务暂不可用");
	}

	@Override
	public Result<EffectiveCodesInnerDTO> getEffectiveCodes(Long userId) {
		log.error("AuthInnerPrincipalFeignClientFallback: getEffectiveCodes failed, userId={}", userId);
		return Result.success(new EffectiveCodesInnerDTO());
	}

	@Override
	public Result<AuthorizationInvalidateResponse> invalidate(AuthorizationInvalidateRequest request) {
		String eventId = request != null ? request.eventId() : null;
		log.error("AuthInnerFeignClientFallback: invalidate failed, eventId={}", eventId);
		return Result.error("Authorization invalidation service is unavailable");
	}

}
