package com.auth.service.auth.controller;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.service.auth.model.vo.inner.EffectiveCodesVO;
import com.auth.service.auth.service.InternalAuthCoordinationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证服务内部接口（缓存刷新、会话踢出等）
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth/inner")
@RestController
public class InternalAuthController {

	private final InternalAuthCoordinationService internalAuthCoordinationService;

	@InternalApi
	@Operation(summary = "刷新用户授权画像缓存（AuthProfile）")
	@PostMapping("/cache/refresh")
	public Result<Void> refreshUserCache(@RequestBody(required = false) List<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return Result.success();
		}
		internalAuthCoordinationService.refreshUserCache(userIds);

		return Result.success();
	}

	@InternalApi
	@Operation(summary = "批量踢出用户全部会话")
	@PostMapping("/sessions/kick-all")
	public Result<Void> kickAllSessions(@RequestBody(required = false) List<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return Result.success();
		}
		internalAuthCoordinationService.kickAllSessions(userIds);

		return Result.success();
	}

	@InternalApi
	@Operation(summary = "查询用户生效角色码与权限码（供 system 动态菜单等内部调用）")
	@GetMapping("/principal/{userId}/effective-codes")
	public Result<EffectiveCodesVO> effectiveCodes(@PathVariable("userId") Long userId) {
		EffectiveCodesVO body = internalAuthCoordinationService.resolveEffectiveCodes(userId);
		return Result.success(body);
	}

}
