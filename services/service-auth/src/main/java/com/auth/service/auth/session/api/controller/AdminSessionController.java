package com.auth.service.auth.session.api.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.session.application.SessionAdminApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端会话管理（踢人等）
 *
 * @author Bunny
 */
@Tag(name = "管理端会话", description = "管理员踢人/会话管理")
@RequestMapping("/api/auth/admin/users")
@RestController
public class AdminSessionController {

	private final SessionAdminApplicationService sessionAdminApplicationService;

	public AdminSessionController(SessionAdminApplicationService sessionAdminApplicationService) {
		this.sessionAdminApplicationService = sessionAdminApplicationService;
	}

	@Operation(summary = "管理员踢下线（按 sessionId/jti）")
	@PreAuthorize("@auth.decide('ADMIN')")
	@PostMapping("{userId}/sessions/{sessionId}/kick")
	public Result<Void> kick(@PathVariable("userId") long userId, @PathVariable("sessionId") String sessionId) {
		sessionAdminApplicationService.kickSession(userId, sessionId);
		return Result.success();
	}

	@Operation(summary = "踢出全部会话")
	@PreAuthorize("@auth.decide('ADMIN')")
	@PostMapping("{userId}/sessions/kick-all")
	public Result<Void> kickAll(@PathVariable("userId") long userId) {
		sessionAdminApplicationService.kickAllSessions(userId);
		return Result.success();
	}

	@Operation(summary = "批量踢出用户全部会话")
	@PostMapping("/kick-all")
	public Result<Void> kickAllSessions(@RequestBody(required = false) List<Long> userIds) {
		sessionAdminApplicationService.kickAllSessions(userIds);
		return Result.success();
	}

	@Operation(summary = "查询用户活跃会话列表")
	@PreAuthorize("@auth.decide('ADMIN')")
	@GetMapping("{userId}/sessions")
	public Result<List<UserSessionIndex>> getListSessions(@PathVariable("userId") long userId) {
		return Result.success(sessionAdminApplicationService.listActiveSessions(userId));
	}

}
