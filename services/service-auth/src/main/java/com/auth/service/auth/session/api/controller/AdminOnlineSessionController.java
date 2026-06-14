package com.auth.service.auth.session.api.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.auth.session.application.SessionAdminApplicationService;
import com.auth.service.auth.session.domain.model.query.OnlineSessionPageQuery;
import com.auth.service.auth.session.domain.model.vo.OnlineSessionPageVO;
import com.auth.service.auth.shared.audit.AuthAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端全局在线会话查询
 *
 * @author Bunny
 */
@Tag(name = "管理端在线会话", description = "全局在线用户会话分页查询")
@RequiredArgsConstructor
@RequestMapping("/api/auth/admin/sessions")
@RestController
public class AdminOnlineSessionController {

	private final SessionAdminApplicationService sessionAdminApplicationService;

	@OperationLog(targetType = "ONLINE_SESSION_LIST", serviceDomain = AuditServiceDomain.AUTH,
			bizModule = AuthAuditBizModule.AUTH_SESSION, operation = OperationLogKind.QUERY)
	@Operation(summary = "分页查询全局在线会话")
	@PreAuthorize("@auth.decide('ADMIN')")
	@GetMapping("/online")
	public Result<PageResponse<OnlineSessionPageVO>> page(OnlineSessionPageQuery query) {
		PageResponse<OnlineSessionPageVO> page = sessionAdminApplicationService.getOnlineSessionPage(query);
		return Result.success(page);
	}

}
