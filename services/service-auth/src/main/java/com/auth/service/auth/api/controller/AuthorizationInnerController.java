package com.auth.service.auth.api.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.api.dto.authorization.request.AuthorizationInvalidationEventQuery;
import com.auth.service.auth.api.dto.authorization.response.AuthorizationInvalidationEventDetailVO;
import com.auth.service.auth.api.dto.authorization.response.AuthorizationInvalidationEventPageVO;
import com.auth.service.auth.api.dto.authorization.response.AuthorizationInvalidationEventStatsVO;
import com.auth.service.auth.api.dto.authorization.response.EffectiveCodesVO;
import com.auth.service.auth.application.authorization.command.AdminAuthorizationService;
import com.auth.service.auth.application.authorization.command.AuthorizationInvalidationEventOpsApplicationService;
import com.auth.service.auth.application.authorization.command.AuthorizationInvalidationService;
import com.auth.service.auth.application.authorization.query.AuthorizationInvalidationEventQueryService;
import com.auth.service.auth.convert.EffectiveCodesConverter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

	private final AdminAuthorizationService adminAuthorizationService;

	private final AuthorizationInvalidationService authorizationInvalidationService;

	private final AuthorizationInvalidationEventQueryService authorizationInvalidationEventQueryService;

	private final AuthorizationInvalidationEventOpsApplicationService authorizationInvalidationEventOpsApplicationService;

	@InternalApi
	@Operation(summary = "查询用户生效角色码与权限码（供 system 动态菜单等内部调用）")
	@GetMapping("/principal/{userId}/effective-codes")
	public Result<EffectiveCodesVO> effectiveCodes(@PathVariable("userId") Long userId) {
		AuthProfile profile = adminAuthorizationService.loadAuthProfile(userId);
		EffectiveCodesVO body = EffectiveCodesConverter.INSTANCE.toVoOrEmpty(profile);
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
		PageResponse<AuthorizationInvalidationEventPageVO> page = authorizationInvalidationEventQueryService
			.getPage(query);
		return Result.success(page);
	}

	@InternalApi
	@Operation(summary = "授权失效幂等事件详情")
	@GetMapping("/invalidation/event/{id}")
	public Result<AuthorizationInvalidationEventDetailVO> invalidationEventDetail(@PathVariable("id") Long id) {
		AuthorizationInvalidationEventDetailVO detailVO = authorizationInvalidationEventQueryService.getDetail(id);
		return Result.success(detailVO);
	}

	@InternalApi
	@Operation(summary = "授权失效幂等事件统计")
	@GetMapping("/invalidation/event/summary")
	public Result<AuthorizationInvalidationEventStatsVO> invalidationEventSummary() {
		AuthorizationInvalidationEventStatsVO statsVO = authorizationInvalidationEventQueryService.getStats();
		return Result.success(statsVO);
	}

	@InternalApi
	@Operation(summary = "释放授权失效幂等事件 processing 占位")
	@PostMapping("/invalidation/event/{eventId}/release-claim")
	public Result<Boolean> releaseInvalidationEventClaim(@PathVariable("eventId") String eventId) {
		boolean released = authorizationInvalidationEventOpsApplicationService.releaseProcessingClaim(eventId);

		return Result.success(released);
	}

}
