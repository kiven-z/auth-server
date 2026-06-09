package com.auth.service.auth.authorization.api.controller;

import com.auth.common.core.model.response.Result;
import com.auth.service.auth.authorization.application.AdminAuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端授权画像运维
 *
 * @author Bunny
 */
@Tag(name = "管理端授权", description = "管理员刷新用户授权画像缓存")
@RequiredArgsConstructor
@RequestMapping("/api/auth/users")
@RestController
public class AdminAuthorizationController {

	private final AdminAuthorizationService adminAuthorizationService;

	@Operation(summary = "批量刷新用户授权画像缓存（AuthProfile）")
	@PreAuthorize("@auth.decide('ADMIN')")
	@PostMapping("/authorization/refresh")
	public Result<Void> refreshAuthorization(@RequestBody(required = false) List<Long> userIds) {
		adminAuthorizationService.refreshUserCache(userIds);
		return Result.success();
	}

}
