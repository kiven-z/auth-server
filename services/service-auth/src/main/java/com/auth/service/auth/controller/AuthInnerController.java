package com.auth.service.auth.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfig.annotation.InternalApi;
import com.auth.service.auth.infrastructure.authorization.AuthorizationImpactQueryService;
import com.auth.service.auth.model.dto.inner.GrantSubjectRequest;
import com.auth.service.auth.model.vo.inner.EffectiveCodesVO;
import com.auth.service.auth.service.AuthInnerService;

import cn.hutool.core.collection.CollUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证服务内部接口（缓存刷新、会话踢出等）
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth/inner")
@RestController
public class AuthInnerController {

	private final AuthInnerService authInnerService;

	private final AuthorizationImpactQueryService authorizationImpactQueryService;

	@InternalApi
	@Operation(summary = "刷新用户授权画像缓存（AuthProfile）")
	@PostMapping("/cache/refresh")
	public Result<Void> refreshUserCache(@RequestBody(required = false) List<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return Result.success();
		}
		authInnerService.refreshUserCache(userIds);

		return Result.success();
	}

	@InternalApi
	@Operation(summary = "批量踢出用户全部会话")
	@PostMapping("/sessions/kick-all")
	public Result<Void> kickAllSessions(@RequestBody(required = false) List<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return Result.success();
		}
		authInnerService.kickAllSessions(userIds);

		return Result.success();
	}

	@InternalApi
	@Operation(summary = "查询用户生效角色码与权限码（供 system 动态菜单等内部调用）")
	@GetMapping("/principal/{userId}/effective-codes")
	public Result<EffectiveCodesVO> effectiveCodes(@PathVariable("userId") Long userId) {
		EffectiveCodesVO body = authInnerService.resolveEffectiveCodes(userId);
		return Result.success(body);
	}

	@InternalApi
	@Operation(summary = "查询持有指定角色的所有用户 ID（单角色，兼容旧调用）")
	@GetMapping("/role/{roleCode}/users")
	public Result<List<Long>> listUserIdsByRoleCode(@PathVariable("roleCode") String roleCode) {
		List<Long> userIds = authorizationImpactQueryService.listUserIdsByRoleCode(roleCode);
		return Result.success(userIds);
	}

	@InternalApi
	@Operation(summary = "按多个角色编码查询受影响用户 ID")
	@PostMapping("/impact/users/by-role-codes")
	public Result<List<Long>> listUserIdsByRoleCodes(@RequestBody(required = false) List<String> roleCodes) {
		return Result.success(authorizationImpactQueryService.listUserIdsByRoleCodes(roleCodes));
	}

	@InternalApi
	@Operation(summary = "按多个权限码查询受影响用户 ID")
	@PostMapping("/impact/users/by-permission-codes")
	public Result<List<Long>> listUserIdsByPermissionCodes(
			@RequestBody(required = false) List<String> permissionCodes) {
		return Result.success(authorizationImpactQueryService.listUserIdsByPermissionCodes(permissionCodes));
	}

	@InternalApi
	@Operation(summary = "按多个策略码查询受影响用户 ID")
	@PostMapping("/impact/users/by-policy-codes")
	public Result<List<Long>> listUserIdsByPolicyCodes(@RequestBody(required = false) List<String> policyCodes) {
		return Result.success(authorizationImpactQueryService.listUserIdsByPolicyCodes(policyCodes));
	}

	@InternalApi
	@Operation(summary = "按部门 ID 列表查询部门下用户 ID")
	@PostMapping("/impact/users/by-dept-ids")
	public Result<List<Long>> listUserIdsByDeptIds(@RequestBody(required = false) List<Long> deptIds) {
		return Result.success(authorizationImpactQueryService.listUserIdsByDeptIds(deptIds));
	}

	@InternalApi
	@Operation(summary = "按岗位 ID 列表查询岗位下用户 ID")
	@PostMapping("/impact/users/by-post-ids")
	public Result<List<Long>> listUserIdsByPostIds(@RequestBody(required = false) List<Long> postIds) {
		return Result.success(authorizationImpactQueryService.listUserIdsByPostIds(postIds));
	}

	@InternalApi
	@Operation(summary = "按授权主体列表（USER/DEPT/POST）合并查询受影响用户 ID")
	@PostMapping("/impact/users/by-grant-subjects")
	public Result<List<Long>> listUserIdsByGrantSubjects(
			@RequestBody(required = false) List<GrantSubjectRequest> subjects) {
		return Result.success(authorizationImpactQueryService.listUserIdsByGrantSubjects(subjects));
	}

}
