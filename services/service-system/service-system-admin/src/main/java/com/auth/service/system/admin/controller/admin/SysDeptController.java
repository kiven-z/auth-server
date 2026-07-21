package com.auth.service.system.admin.controller.admin;

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
import com.auth.service.system.admin.model.query.dept.SysDeptListQuery;
import com.auth.service.system.admin.model.query.dept.SysDeptPageQuery;
import com.auth.service.system.admin.model.vo.dept.SysDeptDetailVO;
import com.auth.service.system.admin.model.vo.dept.SysDeptListVO;
import com.auth.service.system.admin.service.admin.SysDeptQueryService;
import com.auth.service.system.admin.service.admin.SysDeptService;
import com.auth.service.system.authorization.model.constants.AuthorizationAuditBizModule;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@RequestMapping("/api/system/dept")
@RestController
public class SysDeptController {

	private final SysDeptQueryService sysDeptQueryService;

	private final SysDeptService sysDeptService;

	private final SysDeptSpreadsheetService sysDeptSpreadsheetService;

	@Operation(summary = "部门 Excel 导出")
	@PreAuthorize("@auth.decide('sys:dept:export')")
	@GetMapping("/export")
	public ResponseEntity<byte[]> export(@Validated SysDeptPageQuery query, @Validated ExportScopeQueryFields scope)
			throws IOException {
		return sysDeptSpreadsheetService.export(query, scope);
	}

	@Operation(summary = "部门导入模板下载")
	@PreAuthorize("@auth.decide('sys:dept:import')")
	@GetMapping("/import/template")
	public ResponseEntity<byte[]> importTemplate() throws IOException {
		return sysDeptSpreadsheetService.downloadImportTemplate();
	}

	@Operation(summary = "部门 Excel 导入")
	@PreAuthorize("@auth.decide('sys:dept:import')")
	@PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Result<SpreadsheetImportResult> importExcel(@RequestPart("file") MultipartFile file) throws IOException {
		SpreadsheetImportResult data = sysDeptSpreadsheetService.importExcel(file);
		return Result.success(data);
	}

	@OperationLog(targetType = "DEPT_LIST", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.SYS_DEPT, operation = OperationLogKind.QUERY)
	@Operation(summary = "部门扁平列表")
	@PreAuthorize("@auth.decide('sys:dept:query')")
	@GetMapping("/list")
	public Result<List<SysDeptListVO>> list(SysDeptListQuery query) {
		List<SysDeptListVO> data = sysDeptQueryService.listFlat(query);
		return Result.success(data);
	}

	@Operation(summary = "部门分页查询")
	@PreAuthorize("@auth.decide('sys:dept:query')")
	@GetMapping("/page")
	public Result<PageResponse<SysDeptListVO>> page(SysDeptPageQuery query) {
		Page<SysDeptEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<SysDeptListVO> result = sysDeptQueryService.pageFlat(pageParams, query);
		return Result.success(result);
	}

	@Operation(summary = "部门详情")
	@PreAuthorize("@auth.decide('sys:dept:query')")
	@GetMapping("/{id}")
	public Result<SysDeptDetailVO> detail(@PathVariable("id") Long id) {
		SysDeptDetailVO detail = sysDeptQueryService.getDetail(id);
		return Result.success(detail);
	}

	@OperationLog(targetType = "DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.SYS_DEPT, operation = OperationLogKind.CREATE)
	@Operation(summary = "新增部门")
	@PreAuthorize("@auth.decide('sys:dept:create')")
	@PostMapping
	public Result<Void> create(@Validated(CreateGroup.class) @RequestBody SysDeptForm form) {
		sysDeptService.createBatchFromImport(List.of(form));
		return Result.success();
	}

	@OperationLog(targetType = "DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.SYS_DEPT, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新部门")
	@PreAuthorize("@auth.decide('sys:dept:update')")
	@PutMapping
	public Result<Void> update(@Validated(UpdateGroup.class) @RequestBody SysDeptForm form) {
		sysDeptService.updateMeta(form);
		return Result.success();
	}

	@OperationLog(targetType = "DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.SYS_DEPT, operation = OperationLogKind.UPDATE)
	@Operation(summary = "移动部门")
	@PreAuthorize("@auth.decide('sys:dept:update')")
	@PutMapping("/move")
	public Result<Void> move(@Valid @RequestBody SysDeptMoveForm form) {
		sysDeptService.move(form);
		return Result.success();
	}

	@OperationLog(targetType = "DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.SYS_DEPT, operation = OperationLogKind.DELETE)
	@Operation(summary = "删除部门")
	@PreAuthorize("@auth.decide('sys:dept:delete')")
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable("id") Long id) {
		sysDeptService.deleteById(id);
		return Result.success();
	}

}
