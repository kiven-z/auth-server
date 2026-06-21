package com.auth.service.auth.api.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.api.dto.session.response.UserSessionVO;
import com.auth.service.auth.application.session.command.SessionManagementApplicationService;
import com.auth.service.auth.convert.UserSessionConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话域内部接口：服务间批量踢下线与用户会话查询
 *
 * @author Bunny
 */
@Tag(name = "内部会话", description = "服务间会话撤销")
@RequiredArgsConstructor
@RequestMapping("/api/auth/inner/sessions")
@RestController
public class SessionInnerController {

	private final SessionManagementApplicationService sessionManagementApplicationService;

	@InternalApi
	@Operation(summary = "批量踢出用户全部会话")
	@PostMapping("/kick-all")
	public Result<Void> kickAllSessions(@RequestBody(required = false) List<Long> userIds) {
		sessionManagementApplicationService.kickAllSessions(userIds);
		return Result.success();
	}

	@InternalApi
	@Operation(summary = "查询用户活跃会话列表")
	@GetMapping("/users/{userId}/sessions")
	public Result<List<UserSessionVO>> listActiveSessions(@PathVariable("userId") long userId) {
		List<UserSessionIndex> userSessionIndices = sessionManagementApplicationService.listActiveSessions(userId);
		List<UserSessionVO> voList = UserSessionConverter.INSTANCE.toVoList(userSessionIndices);
		return Result.success(voList);
	}

	@InternalApi
	@Operation(summary = "踢出用户指定会话")
	@PostMapping("/users/{userId}/sessions/{sessionId}/kick")
	public Result<Void> kickSession(@PathVariable("userId") long userId, @PathVariable("sessionId") String sessionId) {
		sessionManagementApplicationService.kickSession(userId, sessionId);
		return Result.success();
	}

}
