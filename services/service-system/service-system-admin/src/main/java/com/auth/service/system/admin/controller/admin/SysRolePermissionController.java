package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.form.role.SysRolePermissionAssignForm;
import com.auth.service.system.admin.model.vo.reference.PermissionReferenceVO;
import com.auth.service.system.admin.service.admin.SysRolePermissionService;
import com.auth.service.system.authorization.model.constants.AuthorizationAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 角色权限分配 Controller。
 *
 * @author Bunny
 */
@Tag(name = "角色权限分配")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/system/role/{roleId}/permissions")
@RestController
public class SysRolePermissionController {

	private final SysRolePermissionService sysRolePermissionService;

	@Operation(summary = "查询角色已分配权限码")
	@PreAuthorize("@auth.decide('sys:role:query')")
	@GetMapping
	public Result<List<PermissionReferenceVO>> list(@PathVariable("roleId") Long roleId) {
		List<PermissionReferenceVO> data = sysRolePermissionService.listAssignedPermissions(roleId);
		return Result.success(data);
	}

	@OperationLog(targetType = "ROLE_PERMISSION", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.SYS_ROLE_PERMISSION, operation = OperationLogKind.UPDATE)
	@Operation(summary = "全量分配角色权限码（触发 perm_version 失效）")
	@PreAuthorize("@auth.decide('sys:role:update')")
	@PostMapping
	public Result<Void> assign(@PathVariable("roleId") Long roleId,
			@Valid @RequestBody SysRolePermissionAssignForm form) {
		sysRolePermissionService.assignPermissions(roleId, form.getPermissionIds());
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
