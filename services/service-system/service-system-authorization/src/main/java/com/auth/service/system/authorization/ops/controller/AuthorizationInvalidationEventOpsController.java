package com.auth.service.system.authorization.ops.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.module.security.contract.context.OperationLogContext;
import com.auth.service.system.authorization.ops.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.system.authorization.ops.model.vo.AuthorizationInvalidationEventDetailVO;
import com.auth.service.system.authorization.ops.model.vo.AuthorizationInvalidationEventPageVO;
import com.auth.service.system.authorization.ops.query.AuthorizationInvalidationEventQueryService;
import com.auth.service.system.common.model.SystemAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.auth.service.system.authorization.ops.exception.AuthorizationInvalidationOpsResultCode.EVENT_RELEASE_SUCCESS;

/**
 * 授权失效幂等事件运维查询接口
 *
 * @author Bunny
 */
@Tag(name = "授权失效幂等事件", description = "auth_authorization_invalidation_event")
@RequestMapping("/api/system/ops/authorization-invalidation/event")
@RestController
public class AuthorizationInvalidationEventOpsController {

	private final AuthorizationInvalidationEventQueryService authorizationInvalidationEventQueryService;

	public AuthorizationInvalidationEventOpsController(
			AuthorizationInvalidationEventQueryService authorizationInvalidationEventQueryService) {
		this.authorizationInvalidationEventQueryService = authorizationInvalidationEventQueryService;
	}

	@Operation(summary = "分页查询授权失效幂等事件")
	@PreAuthorize("@auth.decide('sysAuthInvalidationEvent:query')")
	@GetMapping("page")
	public Result<PageResponse<AuthorizationInvalidationEventPageVO>> page(AuthorizationInvalidationEventQuery query) {
		PageResponse<AuthorizationInvalidationEventPageVO> page = authorizationInvalidationEventQueryService
			.getPage(query);
		return Result.success(page);
	}

	@Operation(summary = "授权失效幂等事件详情")
	@PreAuthorize("@auth.decide('sysAuthInvalidationEvent:detail')")
	@GetMapping("{id}")
	public Result<AuthorizationInvalidationEventDetailVO> detail(@PathVariable("id") Long id) {
		AuthorizationInvalidationEventDetailVO detail = authorizationInvalidationEventQueryService.getDetail(id);
		return Result.success(detail);
	}

	@OperationLog(targetType = "AUTH_INVALIDATION_EVENT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.AUTH_INVALIDATION_EVENT, operation = OperationLogKind.UPDATE)
	@Operation(summary = "释放授权失效幂等事件 processing 占位")
	@PreAuthorize("@auth.decide('sysAuthInvalidationEvent:releaseClaim')")
	@PostMapping("{id}/release-claim")
	public Result<Boolean> releaseClaim(@PathVariable("id") Long id) {
		OperationLogContext.setTargetId(id);
		boolean released = authorizationInvalidationEventQueryService.releaseProcessingClaim(id);
		return Result.success(released, EVENT_RELEASE_SUCCESS.getMessage());
	}

}
