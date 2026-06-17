package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignPermissionForm;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignRoleForm;
import com.auth.service.system.admin.model.vo.menu.SysMenuAssignedPermissionRow;
import com.auth.service.system.admin.model.vo.menu.SysMenuAssignedRoleRow;
import com.auth.service.system.admin.service.SysMenuManageService;
import com.auth.service.system.common.model.SystemAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 系统菜单管理
 *
 * @author Bunny
 */
@Tag(name = "系统菜单管理")
@Slf4j
@RequestMapping("/api/system/menu")
@RestController
public class SysMenuManageController {

	private final SysMenuManageService sysMenuManageService;

	public SysMenuManageController(SysMenuManageService sysMenuManageService) {
		this.sysMenuManageService = sysMenuManageService;
	}

	@Operation(summary = "查询菜单已分配角色")
	@PreAuthorize("@auth.decide('sysMenu:query')")
	@GetMapping("/{id}/roles")
	public Result<List<SysMenuAssignedRoleRow>> listRoles(@PathVariable("id") Long id) {
		return Result.success(sysMenuManageService.listAssignedRoles(id));
	}

	@OperationLog(targetType = "MENU_ROLE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_MENU_ROLE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "覆盖分配菜单角色（写入 sys_menu_role）")
	@PreAuthorize("@auth.decide('sysMenu:update')")
	@PutMapping("/{id}/roles")
	public Result<String> replaceRoles(@PathVariable("id") Long id, @Valid @RequestBody SysMenuAssignRoleForm form) {
		sysMenuManageService.replaceMenuRoles(id, form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@Operation(summary = "查询权限列表并标记菜单是否已关联（sys_menu_permission）")
	@PreAuthorize("@auth.decide('sysMenu:query')")
	@GetMapping("/{id}/permissions")
	public Result<List<SysMenuAssignedPermissionRow>> listPermissions(@PathVariable("id") Long id) {
		return Result.success(sysMenuManageService.listPermissionsWithAssignFlags(id));
	}

	@OperationLog(targetType = "MENU_PERMISSION", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_MENU_PERMISSION, operation = OperationLogKind.UPDATE)
	@Operation(summary = "覆盖分配菜单权限（写入 sys_menu_permission）")
	@PreAuthorize("@auth.decide('sysMenu:update')")
	@PutMapping("/{id}/permissions")
	public Result<String> replacePermissions(@PathVariable("id") Long id,
			@Valid @RequestBody SysMenuAssignPermissionForm form) {
		sysMenuManageService.replaceMenuPermissions(id, form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
