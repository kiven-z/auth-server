package com.auth.service.auth.authorization.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.authorization.api.dto.EffectiveCodesVO;
import com.auth.service.auth.authorization.application.AuthorizationInnerService;
import com.auth.service.auth.authorization.application.AuthorizationInvalidationService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
	@Operation(summary = "授权失效（反查用户 → 递增 perm_version → 刷新/驱逐画像）")
	@PostMapping("/invalidate")
	public Result<AuthorizationInvalidateResponse> invalidate(
			@Valid @RequestBody AuthorizationInvalidateRequest request) {
		AuthorizationInvalidateResponse response = authorizationInvalidationService.invalidate(request);
		return Result.success(response);
	}

}
