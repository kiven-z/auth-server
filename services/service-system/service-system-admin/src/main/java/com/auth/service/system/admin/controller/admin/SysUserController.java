package com.auth.service.system.admin.controller.admin;

import java.io.IOException;
import java.util.List;

import com.auth.common.core.model.response.Result;
import com.auth.common.ip.IpAddressService;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.module.file.importer.model.SpreadsheetImportResult;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.excel.user.SysUserSpreadsheetService;
import com.auth.service.system.admin.model.form.user.SysUserAdminResetPasswordForm;
import com.auth.service.system.admin.model.form.user.SysUserBatchStatusForm;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.auth.service.system.admin.service.admin.SysUserService;
import com.auth.service.system.admin.support.user.UserPasswordService;
import com.auth.service.system.common.model.SystemAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
	@PreAuthorize("@auth.decide('sys:user:export')")
	@GetMapping("/export")
	public ResponseEntity<byte[]> export(@Validated SysUserPageQuery filter, @Validated ExportScopeQueryFields scope)
			throws IOException {
		return sysUserSpreadsheetService.export(filter, scope);
	}

	@Operation(summary = "用户导入模板下载")
	@PreAuthorize("@auth.decide('sys:user:import')")
	@GetMapping("/import/template")
	public ResponseEntity<byte[]> importTemplate() throws IOException {
		return sysUserSpreadsheetService.downloadImportTemplate();
	}

	@Operation(summary = "用户 Excel 导入")
	@PreAuthorize("@auth.decide('sys:user:import')")
	@PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Result<SpreadsheetImportResult> importExcel(@RequestPart("file") MultipartFile file) throws IOException {
		SpreadsheetImportResult data = sysUserSpreadsheetService.importExcel(file);
		return Result.success(data);
	}

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.CREATE)
	@Operation(summary = "新增用户")
	@PreAuthorize("@auth.decide('sys:user:create')")
	@PostMapping
	public Result<Void> create(@Validated(CreateGroup.class) @RequestBody SysUserForm form) {
		sysUserService.createBatchFromImport(List.of(form));
		return Result.success();
	}

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.UPDATE)
	@Operation(summary = "管理员重置用户密码")
	@PreAuthorize("@auth.decide('sys:user:update')")
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
	@PreAuthorize("@auth.decide('sys:user:update')")
	@PutMapping
	public Result<Void> update(@Validated(UpdateGroup.class) @RequestBody SysUserForm form) {
		sysUserService.update(form);
		return Result.success();
	}

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.UPDATE)
	@Operation(summary = "批量更新用户状态")
	@PreAuthorize("@auth.decide('sys:user:update')")
	@PutMapping("/status")
	public Result<String> batchUpdateStatus(@Valid @RequestBody SysUserBatchStatusForm form) {
		sysUserService.batchUpdateStatus(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.DELETE)
	@Operation(summary = "批量删除用户")
	@PreAuthorize("@auth.decide('sys:user:delete')")
	@DeleteMapping
	public Result<String> batchDelete(@RequestBody List<Long> ids) {
		sysUserService.deleteByIds(ids);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
