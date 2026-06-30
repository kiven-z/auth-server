package com.auth.service.system.admin.controller.admin;

import java.util.List;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;
import com.auth.service.system.admin.service.admin.SysUserRoleService;
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

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 用户直连角色授权
 *
 * @author Bunny
 */
@Tag(name = "用户角色授权")
@RequiredArgsConstructor
@RequestMapping("/api/system/user-role")
@RestController
public class SysUserRoleController {

	private final SysUserRoleService sysUserRoleService;

	@Operation(summary = "查询用户已分配角色")
	@PreAuthorize("@auth.decide('sys:userrole:query')")
	@GetMapping("/{userId}")
	public Result<List<RoleReferenceVO>> listRoles(@PathVariable("userId") Long userId) {
		List<RoleReferenceVO> rows = sysUserRoleService.listAssignedRoles(userId);
		return Result.success(rows);
	}

	@OperationLog(targetType = "USER_ROLE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.GRANT_TABLE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "全量覆盖用户直连角色授权")
	@PreAuthorize("@auth.decide('sys:userrole:update')")
	@PutMapping("/{userId}")
	public Result<Void> replaceRoles(@PathVariable("userId") Long userId,
			@Valid @RequestBody GrantTableAssignRoleForm form) {
		sysUserRoleService.replaceUserRoles(userId, form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
