package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignRoleForm;
import com.auth.service.system.admin.model.vo.reference.ext.MenuAssignedRoleReferenceVO;
import com.auth.service.system.admin.service.admin.SysMenuManageService;
import com.auth.service.system.authorization.model.constants.AuthorizationAuditBizModule;
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
	@PreAuthorize("@auth.decide('sys:menu:query')")
	@GetMapping("/{id}/roles")
	public Result<List<MenuAssignedRoleReferenceVO>> listRoles(@PathVariable("id") Long id) {
		List<MenuAssignedRoleReferenceVO> data = sysMenuManageService.listAssignedRoles(id);
		return Result.success(data);
	}

	@OperationLog(targetType = "MENU_ROLE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.SYS_MENU_ROLE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "覆盖分配菜单角色（写入 sys_menu_role）")
	@PreAuthorize("@auth.decide('sys:menu:update')")
	@PutMapping("/{id}/roles")
	public Result<String> replaceRoles(@PathVariable("id") Long id, @Valid @RequestBody SysMenuAssignRoleForm form) {
		sysMenuManageService.replaceMenuRoles(id, form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
