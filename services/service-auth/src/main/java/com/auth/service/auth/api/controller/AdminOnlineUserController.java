package com.auth.service.auth.api.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.auth.api.dto.session.request.OnlineUserPageQuery;
import com.auth.service.auth.api.dto.session.response.OnlineUserPageVO;
import com.auth.service.auth.api.dto.session.response.OnlineUserSummaryVO;
import com.auth.service.auth.application.session.query.OnlineUserQueryService;
import com.auth.service.auth.shared.audit.AuthAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端在线用户查询
 *
 * @author Bunny
 */
@Tag(name = "管理端在线用户", description = "在线用户分页查询")
@RequiredArgsConstructor
@RequestMapping("/api/auth/admin/users")
@RestController
public class AdminOnlineUserController {

	private final OnlineUserQueryService onlineUserQueryService;

	@OperationLog(targetType = "ONLINE_USER_LIST", serviceDomain = AuditServiceDomain.AUTH,
			bizModule = AuthAuditBizModule.AUTH_SESSION, operation = OperationLogKind.QUERY)
	@Operation(summary = "分页查询在线用户")
	@PreAuthorize("@auth.decide('ADMIN')")
	@GetMapping("/online")
	public Result<PageResponse<OnlineUserPageVO>> page(OnlineUserPageQuery query) {
		return Result.success(onlineUserQueryService.getOnlineUserPage(query));
	}

	@OperationLog(targetType = "ONLINE_USER_SUMMARY", serviceDomain = AuditServiceDomain.AUTH,
			bizModule = AuthAuditBizModule.AUTH_SESSION, operation = OperationLogKind.QUERY)
	@Operation(summary = "在线用户统计摘要")
	@PreAuthorize("@auth.decide('ADMIN')")
	@GetMapping("/online/summary")
	public Result<OnlineUserSummaryVO> summary() {
		long onlineUserCount = onlineUserQueryService.getOnlineUserCount();

		OnlineUserSummaryVO summary = new OnlineUserSummaryVO();
		summary.setOnlineUserCount(onlineUserCount);

		return Result.success(summary);
	}

}
