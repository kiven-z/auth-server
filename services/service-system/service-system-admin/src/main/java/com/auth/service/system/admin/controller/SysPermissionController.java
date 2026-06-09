package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.excel.permission.SysPermissionSpreadsheetService;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.form.permission.SysPermissionCreateForm;
import com.auth.service.system.admin.model.form.permission.SysPermissionUpdateForm;
import com.auth.service.system.admin.model.query.SysPermissionQuery;
import com.auth.service.system.admin.model.vo.permission.SysPermissionDetailVO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionPageResponse;
import com.auth.service.system.admin.service.SysPermissionService;
import com.auth.service.system.common.model.SystemAuditBizModule;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 系统权限管理 Controller
 *
 * @author Bunny
 */
@Tag(name = "系统权限管理")
@Slf4j
@RequestMapping("/api/system/permission")
@RestController
public class SysPermissionController {

	private final SysPermissionService sysPermissionService;

	private final SysPermissionSpreadsheetService sysPermissionSpreadsheetService;

	public SysPermissionController(SysPermissionService sysPermissionService,
			SysPermissionSpreadsheetService sysPermissionSpreadsheetService) {
		this.sysPermissionService = sysPermissionService;
		this.sysPermissionSpreadsheetService = sysPermissionSpreadsheetService;
	}

	@Operation(summary = "权限 Excel 导出")
	@PreAuthorize("@auth.decide('sysPermission:export')")
	@GetMapping("/export")
	public ResponseEntity<byte[]> export(@Validated SysPermissionQuery filter, @Validated ExportScopeQueryFields scope)
			throws IOException {
		return sysPermissionSpreadsheetService.export(filter, scope);
	}

	@Operation(summary = "权限分页查询")
	@PreAuthorize("@auth.decide('sysPermission:query')")
	@GetMapping("/page")
	public Result<PageResponse<SysPermissionPageResponse>> page(SysPermissionQuery query) {
		Page<SysPermissionEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<SysPermissionPageResponse> result = sysPermissionService.getPage(pageParams, query);
		return Result.success(result);
	}

	@Operation(summary = "权限详情")
	@PreAuthorize("@auth.decide('sysPermission:detail')")
	@GetMapping("/{id}")
	public Result<SysPermissionDetailVO> detail(@PathVariable("id") Long id) {
		SysPermissionDetailVO vo = sysPermissionService.getDetail(id);
		return Result.success(vo);
	}

	@Operation(summary = "新增权限")
	@PreAuthorize("@auth.decide('sysPermission:create')")
	@PostMapping
	public Result<Void> create(@Valid @RequestBody SysPermissionCreateForm form) {
		sysPermissionService.create(form);
		return Result.success();
	}

	@OperationLog(targetType = "PERMISSION", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_PERMISSION, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新权限")
	@PreAuthorize("@auth.decide('sysPermission:update')")
	@PutMapping("/{id}")
	public Result<Void> update(@PathVariable("id") Long id,
			@Validated(UpdateGroup.class) @RequestBody SysPermissionUpdateForm form) {
		form.setId(id);
		sysPermissionService.update(form);
		return Result.success();
	}

	@OperationLog(targetType = "PERMISSION", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_PERMISSION, operation = OperationLogKind.DELETE)
	@Operation(summary = "删除权限")
	@PreAuthorize("@auth.decide('sysPermission:delete')")
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable("id") Long id) {
		sysPermissionService.deleteById(id);
		return Result.success();
	}

}
