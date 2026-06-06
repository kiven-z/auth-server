package com.auth.service.system.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationSummaryVO;
import com.auth.service.system.admin.service.AuthorizationInvalidationStatsAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 授权失效运维统计接口
 *
 * @author Bunny
 */
@Tag(name = "授权失效运维统计", description = "Outbox 与幂等事件统计摘要")
@RequestMapping("/api/system/ops/authorization-invalidation")
@RestController
public class AuthorizationInvalidationStatsAdminController {

	private final AuthorizationInvalidationStatsAdminService authorizationInvalidationStatsAdminService;

	public AuthorizationInvalidationStatsAdminController(
			AuthorizationInvalidationStatsAdminService authorizationInvalidationStatsAdminService) {
		this.authorizationInvalidationStatsAdminService = authorizationInvalidationStatsAdminService;
	}

	@Operation(summary = "授权失效运维统计摘要")
	@PreAuthorize("@auth.decide('sysAuthInvalidationOutbox:query')")
	@GetMapping("summary")
	public Result<AuthorizationInvalidationSummaryVO> summary() {
		return Result.success(authorizationInvalidationStatsAdminService.getSummary());
	}

}
