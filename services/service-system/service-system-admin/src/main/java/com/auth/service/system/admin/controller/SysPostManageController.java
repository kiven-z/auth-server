package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;
import com.auth.service.system.admin.service.GrantTableSubjectRoleService;
import com.auth.service.system.common.model.SystemAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.admin.exception.SystemAdminResultCode.ASSIGN_COMPLETED;

/**
 * 岗位扩展管理
 *
 * @author Bunny
 */
@Tag(name = "岗位扩展管理")
@RequestMapping("/api/system/post")
@RestController
public class SysPostManageController {

	private final GrantTableSubjectRoleService grantTableSubjectRoleService;

	public SysPostManageController(GrantTableSubjectRoleService grantTableSubjectRoleService) {
		this.grantTableSubjectRoleService = grantTableSubjectRoleService;
	}

	@Operation(summary = "查询岗位已分配角色")
	@PreAuthorize("@auth.decide('sysPost:query')")
	@GetMapping("/{postId}/roles")
	public Result<List<GrantTableAssignedRoleRow>> listRoles(@PathVariable("postId") Long postId) {
		return Result.success(grantTableSubjectRoleService.listAssignedRoles(GrantTableSubjectType.POST, postId));
	}

	@OperationLog(targetType = "POST_ROLE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.GRANT_TABLE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "全量覆盖岗位角色授权")
	@PreAuthorize("@auth.decide('sysPost:update')")
	@PutMapping("/{postId}/roles")
	public Result<Void> replaceRoles(@PathVariable("postId") Long postId,
			@Valid @RequestBody GrantTableAssignRoleForm form) {
		grantTableSubjectRoleService.replaceSubjectRoles(GrantTableSubjectType.POST, postId, form);
		return Result.success(null, ASSIGN_COMPLETED.getMessage());
	}

}
