package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.excel.post.SysPostSpreadsheetService;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.model.query.post.SysPostQuery;
import com.auth.service.system.admin.model.vo.post.SysPostDetailVO;
import com.auth.service.system.admin.model.vo.post.SysPostPageResponse;
import com.auth.service.system.admin.service.SysPostService;
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
 * 系统岗位管理 Controller
 *
 * @author Bunny
 */
@Tag(name = "系统岗位管理")
@Slf4j
@RequestMapping("/api/system/post")
@RestController
public class SysPostController {

	private final SysPostService sysPostService;

	private final SysPostSpreadsheetService sysPostSpreadsheetService;

	public SysPostController(SysPostService sysPostService, SysPostSpreadsheetService sysPostSpreadsheetService) {
		this.sysPostService = sysPostService;
		this.sysPostSpreadsheetService = sysPostSpreadsheetService;
	}

	@Operation(summary = "岗位 Excel 导出")
	@PreAuthorize("@auth.decide('sysPost:export')")
	@GetMapping("/export")
	public ResponseEntity<byte[]> export(@Validated SysPostQuery filter, @Validated ExportScopeQueryFields scope)
			throws IOException {
		return sysPostSpreadsheetService.export(filter, scope);
	}

	@Operation(summary = "岗位分页查询")
	@PreAuthorize("@auth.decide('sysPost:query')")
	@GetMapping("/page")
	public Result<PageResponse<SysPostPageResponse>> page(SysPostQuery query) {
		Page<SysPostEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<SysPostPageResponse> result = sysPostService.getPage(pageParams, query);
		return Result.success(result);
	}

	@Operation(summary = "岗位详情")
	@PreAuthorize("@auth.decide('sysPost:query')")
	@GetMapping("/{id}")
	public Result<SysPostDetailVO> detail(@PathVariable("id") Long id) {
		return Result.success(sysPostService.getDetail(id));
	}

	@Operation(summary = "新增岗位")
	@PreAuthorize("@auth.decide('sysPost:create')")
	@PostMapping
	public Result<Void> create(@Valid @RequestBody SysPostForm form) {
		sysPostService.create(form);
		return Result.success();
	}

	@OperationLog(targetType = "POST", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_POST, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新岗位")
	@PreAuthorize("@auth.decide('sysPost:update')")
	@PutMapping("/{id}")
	public Result<Void> update(@PathVariable("id") Long id, @Valid @RequestBody SysPostForm form) {
		sysPostService.update(id, form);
		return Result.success();
	}

	@OperationLog(targetType = "POST", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_POST, operation = OperationLogKind.DELETE)
	@Operation(summary = "删除岗位")
	@PreAuthorize("@auth.decide('sysPost:delete')")
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable("id") Long id) {
		sysPostService.deleteById(id);
		return Result.success();
	}

}
