package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.form.userdept.UserDeptAssignForm;
import com.auth.service.system.admin.model.query.user.UserDeptPageQuery;
import com.auth.service.system.admin.model.vo.userdept.UserDeptPageVO;
import com.auth.service.system.admin.service.SysUserDeptService;
import com.auth.service.system.common.model.SystemAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.*;

/**
 * 用户部门关联管理
 *
 * @author Bunny
 */
@Tag(name = "用户部门关联")
@RequiredArgsConstructor
@RequestMapping("/api/system/user-dept")
@RestController
public class SysUserDeptController {

	private final SysUserDeptService sysUserDeptService;

	@Operation(summary = "分页查询用户部门关联")
	@PreAuthorize("@auth.decide('sysUserDept:query')")
	@GetMapping("/{userId}/page")
	public Result<PageResponse<UserDeptPageVO>> pageDepts(@PathVariable("userId") Long userId,
			UserDeptPageQuery query) {
		PageResponse<UserDeptPageVO> page = sysUserDeptService.getPage(userId, query);
		return Result.success(page);
	}

	@OperationLog(targetType = "USER_DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.CREATE)
	@Operation(summary = "新增用户部门关联")
	@PreAuthorize("@auth.decide('sysUserDept:update')")
	@PostMapping
	public Result<Void> createDept(@Valid @RequestBody UserDeptAssignForm form) {
		sysUserDeptService.create(form);
		return Result.success(null, ADD_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "USER_DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新用户部门关联")
	@PreAuthorize("@auth.decide('sysUserDept:update')")
	@PutMapping
	public Result<Void> updateDept(@Valid @RequestBody UserDeptAssignForm form) {
		sysUserDeptService.update(form);
		return Result.success(null, MODIFY_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "USER_DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.UPDATE)
	@Operation(summary = "设为用户主部门")
	@PreAuthorize("@auth.decide('sysUserDept:update')")
	@PutMapping("/{userId}/{deptId}/primary")
	public Result<Void> setPrimaryDept(@PathVariable("userId") Long userId, @PathVariable("deptId") Long deptId) {
		sysUserDeptService.setPrimary(userId, deptId);
		return Result.success(null, MODIFY_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "USER_DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.UPDATE)
	@Operation(summary = "用户部门调离")
	@PreAuthorize("@auth.decide('sysUserDept:update')")
	@PutMapping("/{userId}/{deptId}/transfer")
	public Result<Void> transferDept(@PathVariable("userId") Long userId, @PathVariable("deptId") Long deptId) {
		sysUserDeptService.transfer(userId, deptId);
		return Result.success(null, MODIFY_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "USER_DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.DELETE)
	@Operation(summary = "批量删除用户部门关联")
	@PreAuthorize("@auth.decide('sysUserDept:update')")
	@DeleteMapping("/{userId}")
	public Result<Void> deleteDepts(@PathVariable("userId") Long userId, @RequestBody List<Long> deptIds) {
		sysUserDeptService.deleteByDeptIds(userId, deptIds);
		return Result.success(null, DELETE_SUCCESS.getMessage());
	}

}
