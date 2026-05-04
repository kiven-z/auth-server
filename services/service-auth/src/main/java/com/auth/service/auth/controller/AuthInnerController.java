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
import com.auth.service.auth.model.vo.inner.EffectiveCodesVO;
import com.auth.service.auth.service.AuthInnerService;

import cn.hutool.core.collection.CollUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证服务内部接口（缓存刷新、会话踢出等
 *
 * @author Bunny
 */
@Slf4j
@RequestMapping("/api/auth/inner")
@RestController
public class AuthInnerController {

	private final AuthInnerService authInnerService;

	public AuthInnerController(AuthInnerService authInnerService) {
		this.authInnerService = authInnerService;
	}

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
	@Operation(summary = "查询持有指定角色的所有用户 ID（供 system 角色变更后缓存刷新用）")
	@GetMapping("/role/{roleCode}/users")
	public Result<List<Long>> listUserIdsByRoleCode(@PathVariable("roleCode") String roleCode) {
		List<Long> userIds = authInnerService.listUserIdsByRoleCode(roleCode);
		return Result.success(userIds);
	}

}
