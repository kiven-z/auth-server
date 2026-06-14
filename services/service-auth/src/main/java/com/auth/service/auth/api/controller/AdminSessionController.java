package com.auth.service.auth.api.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.module.security.contract.context.OperationLogContext;
import com.auth.service.auth.application.session.SessionAdminApplicationService;
import com.auth.service.auth.shared.audit.AuthAuditBizModule;
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

	@OperationLog(targetType = "USER_SESSION", serviceDomain = AuditServiceDomain.AUTH,
			bizModule = AuthAuditBizModule.AUTH_SESSION, operation = OperationLogKind.DELETE)
	@Operation(summary = "管理员踢下线（按 sessionId/jti）")
	@PreAuthorize("@auth.decide('ADMIN')")
	@PostMapping("{userId}/sessions/{sessionId}/kick")
	public Result<Void> kick(@PathVariable("userId") long userId, @PathVariable("sessionId") String sessionId) {
		OperationLogContext.setTargetId(userId);
		sessionAdminApplicationService.kickSession(userId, sessionId);
		return Result.success();
	}

	@OperationLog(targetType = "USER_SESSION", serviceDomain = AuditServiceDomain.AUTH,
			bizModule = AuthAuditBizModule.AUTH_SESSION, operation = OperationLogKind.DELETE)
	@Operation(summary = "踢出全部会话")
	@PreAuthorize("@auth.decide('ADMIN')")
	@PostMapping("{userId}/sessions/kick-all")
	public Result<Void> kickAll(@PathVariable("userId") long userId) {
		OperationLogContext.setTargetId(userId);
		sessionAdminApplicationService.kickAllSessions(userId);
		return Result.success();
	}

	@OperationLog(targetType = "USER_SESSION_LIST", serviceDomain = AuditServiceDomain.AUTH,
			bizModule = AuthAuditBizModule.AUTH_SESSION, operation = OperationLogKind.DELETE)
	@Operation(summary = "批量踢出用户全部会话")
	@PreAuthorize("@auth.decide('ADMIN')")
	@PostMapping("/kick-all")
	public Result<Void> kickAllSessions(@RequestBody(required = false) List<Long> userIds) {
		sessionAdminApplicationService.kickAllSessions(userIds);
		return Result.success();
	}

	@OperationLog(targetType = "USER_SESSION", serviceDomain = AuditServiceDomain.AUTH,
			bizModule = AuthAuditBizModule.AUTH_SESSION, operation = OperationLogKind.QUERY)
	@Operation(summary = "查询用户活跃会话列表")
	@PreAuthorize("@auth.decide('ADMIN')")
	@GetMapping("{userId}/sessions")
	public Result<List<UserSessionIndex>> getListSessions(@PathVariable("userId") long userId) {
		OperationLogContext.setTargetId(userId);
		return Result.success(sessionAdminApplicationService.listActiveSessions(userId));
	}

}
