package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.form.post.SysPostAssignRoleForm;
import com.auth.service.system.admin.model.vo.post.SysPostAssignedRoleRow;
import com.auth.service.system.admin.service.SysPostManageService;
import com.auth.service.system.common.model.audit.SystemAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

	private final SysPostManageService sysPostManageService;

	public SysPostManageController(SysPostManageService sysPostManageService) {
		this.sysPostManageService = sysPostManageService;
	}

	@Operation(summary = "查询岗位已分配角色")
	@PreAuthorize("@auth.decide('sysPost:query')")
	@GetMapping("/{postId}/roles")
	public Result<List<SysPostAssignedRoleRow>> listRoles(@PathVariable("postId") Long postId) {
		return Result.success(sysPostManageService.listAssignedRoles(postId));
	}

	@OperationLog(targetType = "POST_ROLE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.GRANT_TABLE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "全量覆盖岗位角色授权")
	@PreAuthorize("@auth.decide('sysPost:update')")
	@PutMapping("/{postId}/roles")
	public Result<Void> replaceRoles(@PathVariable("postId") Long postId,
			@Valid @RequestBody SysPostAssignRoleForm form) {
		sysPostManageService.replacePostRoles(postId, form);
		return Result.success(null, ASSIGN_COMPLETED.getMessage());
	}

}
