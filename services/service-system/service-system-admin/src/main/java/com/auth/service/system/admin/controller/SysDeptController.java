package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.module.file.importer.model.SpreadsheetImportResult;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.excel.dept.SysDeptSpreadsheetService;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.dept.SysDeptForm;
import com.auth.service.system.admin.model.form.dept.SysDeptMoveForm;
import com.auth.service.system.admin.model.query.SysDeptListQuery;
import com.auth.service.system.admin.model.query.SysDeptPageQuery;
import com.auth.service.system.admin.model.vo.dept.SysDeptDetailVO;
import com.auth.service.system.admin.model.vo.dept.SysDeptListVO;
import com.auth.service.system.admin.service.SysDeptService;
import com.auth.service.system.common.model.SystemAuditBizModule;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 系统部门管理 Controller
 *
 * @author Bunny
 */
@Tag(name = "系统部门管理")
@RequestMapping("/api/system/dept")
@RestController
public class SysDeptController {

	private final SysDeptService sysDeptService;

	private final SysDeptSpreadsheetService sysDeptSpreadsheetService;

	public SysDeptController(SysDeptService sysDeptService, SysDeptSpreadsheetService sysDeptSpreadsheetService) {
		this.sysDeptService = sysDeptService;
		this.sysDeptSpreadsheetService = sysDeptSpreadsheetService;
	}

	@Operation(summary = "部门 Excel 导出")
	@PreAuthorize("@auth.decide('sysDept:export')")
	@GetMapping("/export")
	public ResponseEntity<byte[]> export(@Validated SysDeptPageQuery query, @Validated ExportScopeQueryFields scope)
			throws IOException {
		return sysDeptSpreadsheetService.export(query, scope);
	}

	@Operation(summary = "部门导入模板下载")
	@PreAuthorize("@auth.decide('sysDept:import')")
	@GetMapping("/import/template")
	public ResponseEntity<byte[]> importTemplate() throws IOException {
		return sysDeptSpreadsheetService.downloadImportTemplate();
	}

	@Operation(summary = "部门 Excel 导入")
	@PreAuthorize("@auth.decide('sysDept:import')")
	@PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Result<SpreadsheetImportResult> importExcel(@RequestPart("file") MultipartFile file) throws IOException {
		SpreadsheetImportResult data = sysDeptSpreadsheetService.importExcel(file);
		return Result.success(data);
	}

	@OperationLog(targetType = "DEPT_LIST", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_DEPT, operation = OperationLogKind.QUERY)
	@Operation(summary = "部门扁平列表（全量或关键词过滤，前端建树）")
	@PreAuthorize("@auth.decide('sysDept:query')")
	@GetMapping("/list")
	public Result<List<SysDeptListVO>> list(SysDeptListQuery query) {
		List<SysDeptListVO> data = sysDeptService.listFlat(query);
		return Result.success(data);
	}

	@Operation(summary = "部门分页查询（表格视图）")
	@PreAuthorize("@auth.decide('sysDept:query')")
	@GetMapping("/page")
	public Result<PageResponse<SysDeptListVO>> page(SysDeptPageQuery query) {
		Page<SysDeptEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<SysDeptListVO> result = sysDeptService.pageFlat(pageParams, query);
		return Result.success(result);
	}

	@Operation(summary = "部门详情")
	@PreAuthorize("@auth.decide('sysDept:query')")
	@GetMapping("/{id}")
	public Result<SysDeptDetailVO> detail(@PathVariable("id") Long id) {
		SysDeptDetailVO detail = sysDeptService.getDetail(id);
		return Result.success(detail);
	}

	@OperationLog(targetType = "DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_DEPT, operation = OperationLogKind.CREATE)
	@Operation(summary = "新增部门")
	@PreAuthorize("@auth.decide('sysDept:create')")
	@PostMapping
	public Result<Void> create(@Validated(CreateGroup.class) @RequestBody SysDeptForm form) {
		sysDeptService.create(form);
		return Result.success();
	}

	@OperationLog(targetType = "DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_DEPT, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新部门")
	@PreAuthorize("@auth.decide('sysDept:update')")
	@PutMapping("/{id}")
	public Result<Void> update(@PathVariable("id") Long id,
			@Validated(UpdateGroup.class) @RequestBody SysDeptForm form) {
		form.setId(id);
		sysDeptService.updateMeta(form);
		return Result.success();
	}

	@OperationLog(targetType = "DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_DEPT, operation = OperationLogKind.UPDATE)
	@Operation(summary = "移动部门（变更父部门）")
	@PreAuthorize("@auth.decide('sysDept:update')")
	@PutMapping("/{id}/move")
	public Result<Void> move(@PathVariable("id") Long id, @Valid @RequestBody SysDeptMoveForm form) {
		sysDeptService.move(id, form);
		return Result.success();
	}

	@OperationLog(targetType = "DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_DEPT, operation = OperationLogKind.DELETE)
	@Operation(summary = "删除部门（逻辑删除）")
	@PreAuthorize("@auth.decide('sysDept:delete')")
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable("id") Long id) {
		sysDeptService.deleteById(id);
		return Result.success();
	}

}
