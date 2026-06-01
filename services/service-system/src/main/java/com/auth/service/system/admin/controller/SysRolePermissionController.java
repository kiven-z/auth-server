package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.form.role.SysRolePermissionAssignForm;
import com.auth.service.system.admin.model.vo.role.SysRoleAssignedPermissionRow;
import com.auth.service.system.admin.service.SysRolePermissionService;
import com.auth.service.system.common.model.audit.SystemAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色权限分配 Controller。
 *
 * @author Bunny
 */
@Tag(name = "角色权限分配")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/system/role/{roleCode}/permissions")
@RestController
public class SysRolePermissionController {

	private final SysRolePermissionService sysRolePermissionService;

	@Operation(summary = "查询角色已分配权限码")
	@PreAuthorize("@auth.decide('sysRole:query')")
	@GetMapping
	public Result<List<SysRoleAssignedPermissionRow>> list(@PathVariable("roleCode") String roleCode) {
		return Result.success(sysRolePermissionService.listAssignedPermissions(roleCode));
	}

	@OperationLog(targetType = "ROLE_PERMISSION", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_ROLE_PERMISSION, operation = OperationLogKind.UPDATE)
	@Operation(summary = "全量分配角色权限码（触发 perm_version 失效）")
	@PreAuthorize("@auth.decide('sysRole:update')")
	@PostMapping
	public Result<List<String>> assign(@PathVariable("roleCode") String roleCode,
			@Valid @RequestBody SysRolePermissionAssignForm form) {
		sysRolePermissionService.assignPermissions(roleCode, form.getPermissionCodes());
		List<String> data = sysRolePermissionService.listPermissionCodes(roleCode);
		return Result.success(data);
	}

}
