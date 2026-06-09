package com.auth.service.system.authorization.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.system.authorization.feign.dto.AuthorizationInvalidationEventDetailInnerDTO;
import com.auth.service.system.authorization.feign.dto.AuthorizationInvalidationEventInnerQuery;
import com.auth.service.system.authorization.feign.dto.AuthorizationInvalidationEventPageInnerDTO;
import com.auth.service.system.authorization.feign.dto.AuthorizationInvalidationEventStatsInnerDTO;
import com.auth.service.system.authorization.feign.dto.EffectiveCodesInnerDTO;
import com.auth.service.system.authorization.feign.fallback.AuthInnerFeignClientFallback;

/**
 * 鉴权服务内部 Feign 客户端
 *
 * @author Bunny
 */
@FeignClient(name = "service-auth", contextId = "authInnerFeignClient", path = "/api/auth/inner",
		fallback = AuthInnerFeignClientFallback.class)
public interface AuthInnerFeignClient {

	/**
	 * 刷新用户缓存
	 * @param userIds 用户ID列表
	 * @return 刷新结果
	 */
	@PostMapping("/authorization/cache/refresh")
	Result<String> refreshUserCache(@RequestBody List<Long> userIds);

	/**
	 * 查询用户生效角色码与权限码
	 * @param userId 用户 ID
	 * @return 统一响应体
	 */
	@GetMapping("/authorization/principal/{userId}/effective-codes")
	Result<EffectiveCodesInnerDTO> getEffectiveCodes(@PathVariable("userId") Long userId);

	/**
	 * 触发授权失效与主体画像更新
	 * @param request 授权失效请求
	 * @return 授权失效响应
	 */
	@PostMapping("/authorization/invalidate")
	Result<AuthorizationInvalidateResponse> invalidate(@RequestBody AuthorizationInvalidateRequest request);

	/**
	 * 分页查询授权失效幂等事件
	 * @param query 查询条件
	 * @return 授权失效幂等事件分页响应
	 */
	@GetMapping("/authorization/invalidation/event/page")
	Result<PageResponse<AuthorizationInvalidationEventPageInnerDTO>> getInvalidationEventPage(
			@SpringQueryMap AuthorizationInvalidationEventInnerQuery query);

	/**
	 * 授权失效幂等事件详情
	 * @param id 事件ID
	 * @return 授权失效幂等事件详情
	 */
	@GetMapping("/authorization/invalidation/event/{id}")
	Result<AuthorizationInvalidationEventDetailInnerDTO> getInvalidationEventDetail(@PathVariable("id") Long id);

	/**
	 * 授权失效幂等事件统计
	 * @return 统计摘要
	 */
	@GetMapping("/authorization/invalidation/event/summary")
	Result<AuthorizationInvalidationEventStatsInnerDTO> getInvalidationEventSummary();

	/**
	 * 释放授权失效幂等事件 processing 占位
	 * @param eventId 业务事件 ID
	 * @return 是否实际释放
	 */
	@PostMapping("/authorization/invalidation/event/{eventId}/release-claim")
	Result<Boolean> releaseInvalidationEventClaim(@PathVariable("eventId") String eventId);

	/**
	 * 批量踢出用户全部会话（账户状态变更、删除等）
	 * @param userIds 用户 ID 列表
	 * @return 统一响应
	 */
	@PostMapping("/sessions/kick-all")
	Result<Void> kickAllSessions(@RequestBody(required = false) List<Long> userIds);

}
