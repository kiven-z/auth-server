package com.auth.service.auth.authorization.api.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.authorization.api.dto.EffectiveCodesVO;
import com.auth.service.auth.authorization.application.AuthorizationInnerService;
import com.auth.service.auth.authorization.application.AuthorizationInvalidationService;
import com.auth.service.auth.authorization.application.ops.AuthorizationInvalidationEventOpsApplicationService;
import com.auth.service.auth.authorization.application.query.AuthorizationInvalidationEventQueryApplicationService;
import com.auth.service.auth.authorization.domain.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.auth.authorization.domain.model.vo.AuthorizationInvalidationEventDetailVO;
import com.auth.service.auth.authorization.domain.model.vo.AuthorizationInvalidationEventPageVO;
import com.auth.service.auth.authorization.domain.model.vo.AuthorizationInvalidationEventStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 授权域内部接口
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth/inner/authorization")
@RestController
public class AuthorizationInnerController {

	private final AuthorizationInnerService authorizationInnerService;

	private final AuthorizationInvalidationService authorizationInvalidationService;

	private final AuthorizationInvalidationEventQueryApplicationService authorizationInvalidationEventQueryApplicationService;

	private final AuthorizationInvalidationEventOpsApplicationService authorizationInvalidationEventOpsApplicationService;

	@InternalApi
	@Operation(summary = "查询用户生效角色码与权限码（供 system 动态菜单等内部调用）")
	@GetMapping("/principal/{userId}/effective-codes")
	public Result<EffectiveCodesVO> effectiveCodes(@PathVariable("userId") Long userId) {
		EffectiveCodesVO body = authorizationInnerService.resolveEffectiveCodes(userId);
		return Result.success(body);
	}

	@InternalApi
	@Operation(summary = "授权失效（反查用户 → 递增 perm_version → 刷新/驱逐画像）")
	@PostMapping("/invalidate")
	public Result<AuthorizationInvalidateResponse> invalidate(
			@Valid @RequestBody AuthorizationInvalidateRequest request) {
		AuthorizationInvalidateResponse response = authorizationInvalidationService.invalidate(request);
		return Result.success(response);
	}

	@InternalApi
	@Operation(summary = "分页查询授权失效幂等事件")
	@GetMapping("/invalidation/event/page")
	public Result<PageResponse<AuthorizationInvalidationEventPageVO>> invalidationEventPage(
			AuthorizationInvalidationEventQuery query) {
		return Result.success(authorizationInvalidationEventQueryApplicationService.getPage(query));
	}

	@InternalApi
	@Operation(summary = "授权失效幂等事件详情")
	@GetMapping("/invalidation/event/{id}")
	public Result<AuthorizationInvalidationEventDetailVO> invalidationEventDetail(@PathVariable("id") Long id) {
		return Result.success(authorizationInvalidationEventQueryApplicationService.getDetail(id));
	}

	@InternalApi
	@Operation(summary = "授权失效幂等事件统计")
	@GetMapping("/invalidation/event/summary")
	public Result<AuthorizationInvalidationEventStatsVO> invalidationEventSummary() {
		return Result.success(authorizationInvalidationEventQueryApplicationService.getStats());
	}

	@InternalApi
	@Operation(summary = "释放授权失效幂等事件 processing 占位")
	@PostMapping("/invalidation/event/{eventId}/release-claim")
	public Result<Boolean> releaseInvalidationEventClaim(@PathVariable("eventId") String eventId) {
		boolean released = authorizationInvalidationEventOpsApplicationService.releaseProcessingClaim(eventId);
		return Result.success(released);
	}

}
