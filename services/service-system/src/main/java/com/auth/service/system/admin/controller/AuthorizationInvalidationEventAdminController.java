package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationEventDetailVO;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationEventPageVO;
import com.auth.service.system.admin.service.AuthorizationInvalidationEventAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 授权失效幂等事件运维查询接口
 *
 * @author Bunny
 */
@Tag(name = "授权失效幂等事件", description = "auth_authorization_invalidation_event")
@RequestMapping("/api/system/ops/authorization-invalidation/event")
@RestController
public class AuthorizationInvalidationEventAdminController {

	private final AuthorizationInvalidationEventAdminService authorizationInvalidationEventAdminService;

	public AuthorizationInvalidationEventAdminController(
			AuthorizationInvalidationEventAdminService authorizationInvalidationEventAdminService) {
		this.authorizationInvalidationEventAdminService = authorizationInvalidationEventAdminService;
	}

	@Operation(summary = "分页查询授权失效幂等事件")
	@PreAuthorize("@auth.decide('sysAuthInvalidationEvent:query')")
	@GetMapping("page")
	public Result<PageResponse<AuthorizationInvalidationEventPageVO>> page(AuthorizationInvalidationEventQuery query) {
		PageResponse<AuthorizationInvalidationEventPageVO> page = authorizationInvalidationEventAdminService
			.getPage(query);
		return Result.success(page);
	}

	@Operation(summary = "授权失效幂等事件详情")
	@PreAuthorize("@auth.decide('sysAuthInvalidationEvent:detail')")
	@GetMapping("{id}")
	public Result<AuthorizationInvalidationEventDetailVO> detail(@PathVariable("id") Long id) {
		AuthorizationInvalidationEventDetailVO detail = authorizationInvalidationEventAdminService.getDetail(id);
		return Result.success(detail);
	}

}
