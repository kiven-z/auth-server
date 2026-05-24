package com.auth.service.auth.authorization.api.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.service.auth.authorization.api.dto.EffectiveCodesVO;
import com.auth.service.auth.authorization.api.dto.invalidation.InvalidationResponse;
import com.auth.service.auth.authorization.api.dto.invalidation.RoleInvalidateRequest;
import com.auth.service.auth.authorization.application.AuthorizationInnerService;
import com.auth.service.auth.authorization.domain.invalidation.model.ImpactTarget;
import com.auth.service.auth.authorization.domain.invalidation.model.InvalidationCommand;
import com.auth.service.auth.authorization.domain.invalidation.model.RoleCodes;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 授权域内部接口（画像缓存刷新、生效码查询）。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth/inner/authorization")
@RestController
public class AuthorizationInnerController {

	private final AuthorizationInnerService authorizationInnerService;

	@InternalApi
	@Operation(summary = "刷新用户授权画像缓存（AuthProfile）")
	@PostMapping("/cache/refresh")
	public Result<Void> refreshUserCache(@RequestBody(required = false) List<Long> userIds) {
		authorizationInnerService.refreshUserCache(userIds);
		return Result.success();
	}

	@InternalApi
	@Operation(summary = "查询用户生效角色码与权限码（供 system 动态菜单等内部调用）")
	@GetMapping("/principal/{userId}/effective-codes")
	public Result<EffectiveCodesVO> effectiveCodes(@PathVariable("userId") Long userId) {
		EffectiveCodesVO body = authorizationInnerService.resolveEffectiveCodes(userId);
		return Result.success(body);
	}

	@InternalApi
	@Operation(summary = "按角色码失效授权（反查用户 → 递增 perm_version → 刷新/驱逐画像）")
	@PostMapping("/invalidate/role")
	public Result<InvalidationResponse> invalidateByRole(@Valid @RequestBody RoleInvalidateRequest request) {
		ImpactTarget impactTarget = new RoleCodes(request.getRoleCodes());
		String eventId = request.getEventId();

		// TODO 待实现
		InvalidationCommand command = new InvalidationCommand(impactTarget, eventId);

		return Result.success();
	}

}
