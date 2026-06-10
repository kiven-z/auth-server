package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;
import com.auth.service.system.admin.service.SysUserManageService;
import com.auth.service.system.common.model.SystemAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
 * 用户扩展管理
 *
 * @author Bunny
 */
@Tag(name = "用户扩展管理")
@RequestMapping("/api/system/user")
@RestController
@RequiredArgsConstructor
public class SysUserManageController {

	private final SysUserManageService sysUserManageService;

	@Operation(summary = "查询用户已分配角色")
	@PreAuthorize("@auth.decide('sysUser:query')")
	@GetMapping("/{userId}/roles")
	public Result<List<GrantTableAssignedRoleRow>> listRoles(@PathVariable("userId") Long userId) {
		return Result.success(sysUserManageService.listAssignedRoles(userId));
	}

	@OperationLog(targetType = "USER_ROLE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.GRANT_TABLE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "全量覆盖用户直连角色授权")
	@PreAuthorize("@auth.decide('sysUser:update')")
	@PutMapping("/{userId}/roles")
	public Result<Void> replaceRoles(@PathVariable("userId") Long userId,
			@Valid @RequestBody GrantTableAssignRoleForm form) {
		sysUserManageService.replaceUserRoles(userId, form);
		return Result.success(null, ASSIGN_COMPLETED.getMessage());
	}

}
