package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.ip.IpAddressService;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.excel.user.SysUserSpreadsheetService;
import com.auth.service.system.admin.model.form.user.SysUserAdminResetPasswordForm;
import com.auth.service.system.admin.model.form.user.SysUserBatchStatusForm;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.auth.service.system.admin.model.vo.user.SysUserPageVO;
import com.auth.service.system.admin.service.admin.SysUserService;
import com.auth.service.system.admin.support.user.UserPasswordService;
import com.auth.service.system.common.model.SystemAuditBizModule;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 系统用户管理 Controller
 *
 * @author Bunny
 */
@Tag(name = "系统用户管理")
@RequiredArgsConstructor
@RequestMapping("/api/system/user")
@RestController
public class SysUserController {

	private final SysUserService sysUserService;

	private final SysUserSpreadsheetService sysUserSpreadsheetService;

	private final UserPasswordService userPasswordService;

	private final IpAddressService ipAddressService;

	@Operation(summary = "用户 Excel 导出")
	@PreAuthorize("@auth.decide('sysUser:export')")
	@GetMapping("/export")
	public ResponseEntity<byte[]> export(@Validated SysUserPageQuery filter, @Validated ExportScopeQueryFields scope)
			throws IOException {
		return sysUserSpreadsheetService.export(filter, scope);
	}

	@OperationLog(targetType = "USER_LIST", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.QUERY)
	@Operation(summary = "用户分页查询")
	@PreAuthorize("@auth.decide('sysUser:query')")
	@GetMapping("/page")
	public Result<PageResponse<SysUserPageVO>> page(SysUserPageQuery query) {
		Page<UserEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<SysUserPageVO> result = sysUserService.getPage(pageParams, query);
		return Result.success(result);
	}

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.CREATE)
	@Operation(summary = "新增用户")
	@PreAuthorize("@auth.decide('sysUser:create')")
	@PostMapping
	public Result<Void> create(@Validated(CreateGroup.class) @RequestBody SysUserForm form) {
		sysUserService.create(form);
		return Result.success();
	}

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.UPDATE)
	@Operation(summary = "管理员重置用户密码")
	@PreAuthorize("@auth.decide('sysUser:update')")
	@PutMapping("/{id}/password")
	public Result<String> resetPassword(@PathVariable("id") Long id,
			@Valid @RequestBody SysUserAdminResetPasswordForm form, HttpServletRequest request) {
		String changeIp = ipAddressService.resolveIpInfo(request).getIpAddr();
		userPasswordService.resetPasswordByAdmin(id, form, changeIp);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新用户基础资料")
	@PreAuthorize("@auth.decide('sysUser:update')")
	@PutMapping("/{id}")
	public Result<Void> update(@PathVariable("id") Long id,
			@Validated(UpdateGroup.class) @RequestBody SysUserForm form) {
		sysUserService.update(id, form);
		return Result.success();
	}

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.UPDATE)
	@Operation(summary = "批量更新用户状态")
	@PreAuthorize("@auth.decide('sysUser:update')")
	@PutMapping("/status")
	public Result<String> batchUpdateStatus(@Valid @RequestBody SysUserBatchStatusForm form) {
		sysUserService.batchUpdateStatus(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.DELETE)
	@Operation(summary = "批量删除用户")
	@PreAuthorize("@auth.decide('sysUser:delete')")
	@DeleteMapping
	public Result<String> batchDelete(@RequestBody List<Long> ids) {
		sysUserService.deleteByIds(ids);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
