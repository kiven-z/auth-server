package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.excel.role.SysRoleSpreadsheetService;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleForm;
import com.auth.service.system.admin.model.query.role.SysRoleQuery;
import com.auth.service.system.admin.model.vo.role.SysRoleDetailVO;
import com.auth.service.system.admin.model.vo.role.SysRoleOptionVO;
import com.auth.service.system.admin.model.vo.role.SysRolePageVO;
import com.auth.service.system.admin.service.admin.SysRoleQueryService;
import com.auth.service.system.admin.service.admin.SysRoleService;
import com.auth.service.system.common.model.SystemAuditBizModule;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * 系统角色管理 Controller
 *
 * @author Bunny
 */
@Tag(name = "系统角色管理")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/system/role")
@RestController
public class SysRoleController {

	private final SysRoleQueryService sysRoleQueryService;

	private final SysRoleService sysRoleService;

	private final SysRoleSpreadsheetService sysRoleSpreadsheetService;

	@Operation(summary = "角色 Excel 导出")
	@PreAuthorize("@auth.decide('sysRole:export')")
	@GetMapping("/export")
	public ResponseEntity<byte[]> export(@Validated SysRoleQuery filter, @Validated ExportScopeQueryFields scope)
			throws IOException {
		return sysRoleSpreadsheetService.export(filter, scope);
	}

	@Operation(summary = "角色分页查询")
	@PreAuthorize("@auth.decide('sysRole:query')")
	@GetMapping("/page")
	public Result<PageResponse<SysRolePageVO>> page(SysRoleQuery query) {
		Page<SysRoleEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<SysRolePageVO> result = sysRoleQueryService.getPage(pageParams, query);
		return Result.success(result);
	}

	@Operation(summary = "角色详情")
	@PreAuthorize("@auth.decide('sysRole:detail')")
	@GetMapping("/{id}")
	public Result<SysRoleDetailVO> detail(@PathVariable("id") Long id) {
		SysRoleDetailVO vo = sysRoleQueryService.getDetail(id);
		return Result.success(vo);
	}

	@Operation(summary = "新增角色")
	@PreAuthorize("@auth.decide('sysRole:create')")
	@PostMapping
	public Result<Void> create(@Validated(CreateGroup.class) @RequestBody SysRoleForm form) {
		sysRoleService.create(form);
		return Result.success();
	}

	@OperationLog(targetType = "ROLE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_ROLE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新角色")
	@PreAuthorize("@auth.decide('sysRole:update')")
	@PutMapping("/{id}")
	public Result<Void> update(@PathVariable("id") Long id,
			@Validated(UpdateGroup.class) @RequestBody SysRoleForm form) {
		form.setId(id);
		sysRoleService.update(form);
		return Result.success();
	}

	@OperationLog(targetType = "ROLE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_ROLE, operation = OperationLogKind.DELETE)
	@Operation(summary = "删除角色")
	@PreAuthorize("@auth.decide('sysRole:delete')")
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable("id") Long id) {
		sysRoleService.deleteById(id);
		return Result.success();
	}

	@Operation(summary = "角色下拉选项")
	@PreAuthorize("@auth.decide('sysRole:query')")
	@GetMapping("/options")
	public Result<List<SysRoleOptionVO>> options(@RequestParam(value = "roleName", required = false) String roleName,
			@RequestParam(value = "roleCode", required = false) String roleCode) {
		List<SysRoleOptionVO> list = sysRoleQueryService.listOptions(roleName, roleCode);
		return Result.success(list);
	}

}
