package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuBatchStatusForm;
import com.auth.service.system.admin.model.form.menu.SysMenuSaveForm;
import com.auth.service.system.admin.model.query.SysMenuQuery;
import com.auth.service.system.admin.model.vo.menu.RouteNodeVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuDetailVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuListVO;
import com.auth.service.system.admin.service.SysMenuService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.*;

/**
 * 菜单 Web 端接口（动态路由等）
 *
 * @author Bunny
 */
@Tag(name = "系统菜单-Web", description = "动态路由（门户）")
@RequestMapping("/api/system/menu")
@RestController
public class SysMenuController {

	private final SysMenuService sysMenuService;

	public SysMenuController(SysMenuService sysMenuService) {
		this.sysMenuService = sysMenuService;
	}

	@AuthenticatedApi
	@Operation(summary = "动态路由列表（占位返回空）")
	@GetMapping("/web-routes")
	public Result<List<RouteNodeVO>> getWebRoutes() {
		Long userId = SecurityUserUtils.getUserId();
		List<RouteNodeVO> menuList = sysMenuService.listWebRoutes(userId);
		return Result.success(menuList);
	}

	@Operation(summary = "菜单扁平列表（全量，前端建树）")
	@PreAuthorize("@auth.decide('sysMenu:query')")
	@GetMapping("/list")
	public Result<List<SysMenuListVO>> list(SysMenuQuery query) {
		return Result.success(sysMenuService.listFlat(query));
	}

	@Operation(summary = "菜单分页查询（表格视图）")
	@PreAuthorize("@auth.decide('sysMenu:query')")
	@GetMapping("/page")
	public Result<PageResponse<SysMenuListVO>> page(SysMenuQuery query) {
		Page<SysMenuEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<SysMenuListVO> result = sysMenuService.pageFlat(pageParams, query);
		return Result.success(result);
	}

	@Operation(summary = "菜单详情")
	@PreAuthorize("@auth.decide('sysMenu:query')")
	@GetMapping("/{id}")
	public Result<SysMenuDetailVO> detail(@PathVariable("id") Long id) {
		SysMenuDetailVO detail = sysMenuService.getDetail(id);
		return Result.success(detail);
	}

	@Operation(summary = "新增菜单")
	@PreAuthorize("@auth.decide('sysMenu:create')")
	@PostMapping
	public Result<String> create(@Valid @RequestBody SysMenuSaveForm form) {
		String id = sysMenuService.create(form);
		return Result.success(id, ADD_SUCCESS.getMessage());
	}

	@Operation(summary = "更新菜单")
	@PreAuthorize("@auth.decide('sysMenu:update')")
	@PutMapping
	public Result<String> update(@Validated(UpdateGroup.class) @RequestBody SysMenuSaveForm form) {
		sysMenuService.update(form);
		return Result.success(null, MODIFY_SUCCESS.getMessage());
	}

	@Operation(summary = "批量删除菜单（逻辑删除）")
	@PreAuthorize("@auth.decide('sysMenu:delete')")
	@DeleteMapping
	public Result<String> remove(@RequestBody List<Long> ids) {
		sysMenuService.removeBatchByIds(ids);
		return Result.success(null, DELETE_SUCCESS.getMessage());
	}

	@Operation(summary = "批量更新菜单状态（0=启用 1=禁用）")
	@PreAuthorize("@auth.decide('sysMenu:update')")
	@PutMapping("/status")
	public Result<String> batchUpdateStatus(@Valid @RequestBody SysMenuBatchStatusForm form) {
		sysMenuService.batchUpdateStatus(form);
		return Result.success(null, MODIFY_SUCCESS.getMessage());
	}

}
