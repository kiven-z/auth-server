package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.dept.SysDeptCreateForm;
import com.auth.service.system.admin.model.form.dept.SysDeptMoveForm;
import com.auth.service.system.admin.model.form.dept.SysDeptUpdateForm;
import com.auth.service.system.admin.model.query.SysDeptQuery;
import com.auth.service.system.admin.model.vo.dept.SysDeptDetailVO;
import com.auth.service.system.admin.model.vo.dept.SysDeptListVO;
import com.auth.service.system.admin.service.SysDeptCommandService;
import com.auth.service.system.admin.service.SysDeptService;
import com.auth.service.system.common.model.audit.SystemAuditBizModule;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统部门管理 Controller（查询）。
 *
 * @author Bunny
 */
@Tag(name = "系统部门管理")
@RequestMapping("/api/system/dept")
@RestController
public class SysDeptController {

	private final SysDeptService sysDeptService;

	private final SysDeptCommandService sysDeptCommandService;

	public SysDeptController(SysDeptService sysDeptService, SysDeptCommandService sysDeptCommandService) {
		this.sysDeptService = sysDeptService;
		this.sysDeptCommandService = sysDeptCommandService;
	}

	@OperationLog(targetType = "DEPT_LIST", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_DEPT, operation = OperationLogKind.QUERY)
	@Operation(summary = "部门扁平列表（全量，前端建树）")
	@PreAuthorize("@auth.decide('sysDept:query')")
	@GetMapping("/list")
	public Result<List<SysDeptListVO>> list(SysDeptQuery query) {
		return Result.success(sysDeptService.listFlat(query));
	}

	@Operation(summary = "部门分页查询（表格视图）")
	@PreAuthorize("@auth.decide('sysDept:query')")
	@GetMapping("/page")
	public Result<PageResponse<SysDeptListVO>> page(SysDeptQuery query) {
		Page<SysDeptEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<SysDeptListVO> result = sysDeptService.pageFlat(pageParams, query);
		return Result.success(result);
	}

	@Operation(summary = "部门详情")
	@PreAuthorize("@auth.decide('sysDept:query')")
	@GetMapping("/{id}")
	public Result<SysDeptDetailVO> detail(@PathVariable("id") Long id) {
		return Result.success(sysDeptService.getDetail(id));
	}

	@OperationLog(targetType = "DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_DEPT, operation = OperationLogKind.CREATE)
	@Operation(summary = "新增部门")
	@PreAuthorize("@auth.decide('sysDept:create')")
	@PostMapping
	public Result<Void> create(@Valid @RequestBody SysDeptCreateForm form) {
		sysDeptCommandService.create(form);
		return Result.success();
	}

	@OperationLog(targetType = "DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_DEPT, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新部门（不含父部门）")
	@PreAuthorize("@auth.decide('sysDept:update')")
	@PutMapping("/{id}")
	public Result<Void> update(@PathVariable("id") Long id,
			@Validated(UpdateGroup.class) @RequestBody SysDeptUpdateForm form) {
		form.setId(id);
		sysDeptCommandService.updateMeta(form);
		return Result.success();
	}

	@OperationLog(targetType = "DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_DEPT, operation = OperationLogKind.UPDATE)
	@Operation(summary = "移动部门（变更父部门）")
	@PreAuthorize("@auth.decide('sysDept:update')")
	@PutMapping("/{id}/move")
	public Result<Void> move(@PathVariable("id") Long id, @Valid @RequestBody SysDeptMoveForm form) {
		sysDeptCommandService.move(id, form);
		return Result.success();
	}

	@OperationLog(targetType = "DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_DEPT, operation = OperationLogKind.DELETE)
	@Operation(summary = "删除部门（逻辑删除）")
	@PreAuthorize("@auth.decide('sysDept:delete')")
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable("id") Long id) {
		sysDeptCommandService.deleteById(id);
		return Result.success();
	}

}
