package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.AuthorizationInvalidationOutboxQuery;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationOutboxDetailVO;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationOutboxPageVO;
import com.auth.service.system.admin.service.AuthorizationInvalidationOutboxAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 授权失效 Outbox
 *
 * @author Bunny
 */
@Tag(name = "授权失效 Outbox", description = "sys_authorization_invalidation_outbox")
@RequestMapping("/api/system/ops/authorization-invalidation/outbox")
@RestController
public class AuthorizationInvalidationOutboxAdminController {

	private final AuthorizationInvalidationOutboxAdminService authorizationInvalidationOutboxAdminService;

	public AuthorizationInvalidationOutboxAdminController(
			AuthorizationInvalidationOutboxAdminService authorizationInvalidationOutboxAdminService) {
		this.authorizationInvalidationOutboxAdminService = authorizationInvalidationOutboxAdminService;
	}

	@Operation(summary = "分页查询授权失效 Outbox")
	@PreAuthorize("@auth.decide('sysAuthInvalidationOutbox:query')")
	@GetMapping("page")
	public Result<PageResponse<AuthorizationInvalidationOutboxPageVO>> page(
			AuthorizationInvalidationOutboxQuery query) {
		PageResponse<AuthorizationInvalidationOutboxPageVO> page = authorizationInvalidationOutboxAdminService
			.getPage(query);
		return Result.success(page);
	}

	@Operation(summary = "授权失效 Outbox 详情")
	@PreAuthorize("@auth.decide('sysAuthInvalidationOutbox:detail')")
	@GetMapping("{id}")
	public Result<AuthorizationInvalidationOutboxDetailVO> detail(@PathVariable("id") Long id) {
		AuthorizationInvalidationOutboxDetailVO detail = authorizationInvalidationOutboxAdminService.getDetail(id);
		return Result.success(detail);
	}

}
