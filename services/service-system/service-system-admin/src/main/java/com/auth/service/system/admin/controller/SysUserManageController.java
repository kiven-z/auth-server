package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.form.userdept.UserDeptAssignForm;
import com.auth.service.system.admin.model.query.user.UserDeptPageQuery;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;
import com.auth.service.system.admin.model.vo.userdept.UserDeptPageVO;
import com.auth.service.system.admin.service.SysUserDeptService;
import com.auth.service.system.admin.service.SysUserManageService;
import com.auth.service.system.common.model.SystemAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.admin.exception.SystemAdminResultCode.ASSIGN_COMPLETED;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.*;

/**
 * 用户扩展管理
 *
 * @author Bunny
 */
@Tag(name = "用户扩展管理")
@RequiredArgsConstructor
@RequestMapping("/api/system/user")
@RestController
public class SysUserManageController {

	private final SysUserManageService sysUserManageService;

	private final SysUserDeptService sysUserDeptService;

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

	@Operation(summary = "分页查询用户部门关联")
	@PreAuthorize("@auth.decide('sysUser:query')")
	@GetMapping("/{userId}/depts/page")
	public Result<PageResponse<UserDeptPageVO>> pageDepts(@PathVariable("userId") Long userId,
			UserDeptPageQuery query) {
		PageResponse<UserDeptPageVO> page = sysUserDeptService.getPage(userId, query);
		return Result.success(page);
	}

	@OperationLog(targetType = "USER_DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.CREATE)
	@Operation(summary = "新增用户部门关联")
	@PreAuthorize("@auth.decide('sysUser:update')")
	@PostMapping("/depts")
	public Result<Void> createDept(@Valid @RequestBody UserDeptAssignForm form) {
		sysUserDeptService.create(form);
		return Result.success(null, ADD_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "USER_DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新用户部门关联")
	@PreAuthorize("@auth.decide('sysUser:update')")
	@PutMapping("/depts")
	public Result<Void> updateDept(@Valid @RequestBody UserDeptAssignForm form) {
		sysUserDeptService.update(form);
		return Result.success(null, MODIFY_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "USER_DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.UPDATE)
	@Operation(summary = "设为用户主部门")
	@PreAuthorize("@auth.decide('sysUser:update')")
	@PutMapping("/{userId}/depts/{deptId}/primary")
	public Result<Void> setPrimaryDept(@PathVariable("userId") Long userId, @PathVariable("deptId") Long deptId) {
		sysUserDeptService.setPrimary(userId, deptId);
		return Result.success(null, MODIFY_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "USER_DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.UPDATE)
	@Operation(summary = "用户部门调离")
	@PreAuthorize("@auth.decide('sysUser:update')")
	@PutMapping("/{userId}/depts/{deptId}/transfer")
	public Result<Void> transferDept(@PathVariable("userId") Long userId, @PathVariable("deptId") Long deptId) {
		sysUserDeptService.transfer(userId, deptId);
		return Result.success(null, MODIFY_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "USER_DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.DELETE)
	@Operation(summary = "批量删除用户部门关联")
	@PreAuthorize("@auth.decide('sysUser:update')")
	@DeleteMapping("/{userId}/depts")
	public Result<String> deleteDepts(@PathVariable("userId") Long userId, @RequestBody List<Long> deptIds) {
		sysUserDeptService.deleteByDeptIds(userId, deptIds);
		return Result.success(null, DELETE_SUCCESS.getMessage());
	}

}
