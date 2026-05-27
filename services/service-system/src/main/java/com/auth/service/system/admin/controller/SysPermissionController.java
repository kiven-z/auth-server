package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.form.permission.SysPermissionCreateForm;
import com.auth.service.system.admin.model.query.SysPermissionQuery;
import com.auth.service.system.admin.model.vo.permission.SysPermissionPageResponse;
import com.auth.service.system.admin.service.SysPermissionService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

	public SysPermissionController(SysPermissionService sysPermissionService) {
		this.sysPermissionService = sysPermissionService;
	}

	@Operation(summary = "权限分页查询")
	@PreAuthorize("@auth.decide('sysPermission:query')")
	@GetMapping("/page")
	public Result<PageResponse<SysPermissionPageResponse>> page(SysPermissionQuery query) {
		Page<SysPermissionEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<SysPermissionPageResponse> result = sysPermissionService.getPage(pageParams, query);
		return Result.success(result);
	}

	@Operation(summary = "新增权限")
	@PreAuthorize("@auth.decide('sysPermission:create')")
	@PostMapping
	public Result<Void> create(@Valid @RequestBody SysPermissionCreateForm form) {
		sysPermissionService.create(form);
		return Result.success();
	}

	@Operation(summary = "批量删除权限")
	@PreAuthorize("@auth.decide('sysPermission:delete')")
	@DeleteMapping
	public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
		sysPermissionService.deleteByIds(ids);
		return Result.success();
	}

}
