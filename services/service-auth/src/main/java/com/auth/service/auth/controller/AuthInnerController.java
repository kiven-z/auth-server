package com.auth.service.auth.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfig.annotation.InternalApi;
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

}
