package com.auth.service.system.admin.controller;

import static com.auth.service.system.admin.exception.SystemAdminResultCode.AUTH_INVALIDATION_OUTBOX_RETRY_FAILED;
import static com.auth.service.system.admin.exception.SystemAdminResultCode.AUTH_INVALIDATION_OUTBOX_RETRY_SUCCESS;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.module.security.contract.context.OperationLogContext;
import com.auth.service.system.admin.model.form.authorizationinvalidation.AuthorizationInvalidationOutboxRetryForm;
import com.auth.service.system.admin.model.query.AuthorizationInvalidationOutboxQuery;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationOutboxDetailVO;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationOutboxPageVO;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationOutboxRetryResultVO;
import com.auth.service.system.admin.service.AuthorizationInvalidationOutboxAdminService;
import com.auth.service.system.authorization.outbox.AuthorizationInvalidationOutboxManualRetryService;
import com.auth.service.system.common.model.audit.SystemAuditBizModule;

import cn.hutool.core.util.BooleanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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

	private final AuthorizationInvalidationOutboxManualRetryService authorizationInvalidationOutboxManualRetryService;

	public AuthorizationInvalidationOutboxAdminController(
			AuthorizationInvalidationOutboxAdminService authorizationInvalidationOutboxAdminService,
			AuthorizationInvalidationOutboxManualRetryService authorizationInvalidationOutboxManualRetryService) {
		this.authorizationInvalidationOutboxAdminService = authorizationInvalidationOutboxAdminService;
		this.authorizationInvalidationOutboxManualRetryService = authorizationInvalidationOutboxManualRetryService;
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

	@OperationLog(targetType = "AUTH_INVALIDATION_OUTBOX", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.AUTH_INVALIDATION_OUTBOX, operation = OperationLogKind.UPDATE)
	@Operation(summary = "人工重试授权失效 Outbox 投递")
	@PreAuthorize("@auth.decide('sysAuthInvalidationOutbox:retry')")
	@PostMapping("{id}/retry")
	public Result<AuthorizationInvalidationOutboxRetryResultVO> retry(@PathVariable("id") Long id,
			@RequestBody(required = false) AuthorizationInvalidationOutboxRetryForm form) {
		// 设置操作日志上下文
		OperationLogContext.setTargetId(id);

		// 人工重试 Outbox
		AuthorizationInvalidationOutboxRetryResultVO resultVO = authorizationInvalidationOutboxManualRetryService
			.retryById(id, form);
		if (BooleanUtil.isTrue(resultVO.getDispatched())) {
			return Result.success(resultVO, AUTH_INVALIDATION_OUTBOX_RETRY_SUCCESS.getMessage());
		}
		return Result.success(resultVO, AUTH_INVALIDATION_OUTBOX_RETRY_FAILED.getMessage());
	}

}
