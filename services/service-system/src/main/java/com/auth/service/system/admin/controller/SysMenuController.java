package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignPermissionForm;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignRoleForm;
import com.auth.service.system.admin.model.form.menu.SysMenuBatchStatusForm;
import com.auth.service.system.admin.model.form.menu.SysMenuSaveForm;
import com.auth.service.system.admin.model.query.SysMenuListQuery;
import com.auth.service.system.admin.model.query.SysMenuPageQuery;
import com.auth.service.system.admin.model.vo.menu.SysMenuAssignedPermissionRow;
import com.auth.service.system.admin.model.vo.menu.SysMenuAssignedRoleRow;
import com.auth.service.system.admin.model.vo.menu.SysMenuDetailVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuListVO;
import com.auth.service.system.admin.service.SysMenuManageService;
import com.auth.service.system.admin.service.SysMenuService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.SystemResultCodeEnum.*;

/**
 * 系统菜单管理（列表 / 分页 / CRUD / 分配角色与权限；与 {@code /api/system/menu/web-routes} 区分）
 *
 * @author Bunny
 */
@Tag(name = "系统菜单管理")
@Slf4j
@RequestMapping("/api/system/menu")
@RestController
public class SysMenuController {

	private final SysMenuService sysMenuService;

	private final SysMenuManageService sysMenuManageService;

	public SysMenuController(SysMenuService sysMenuService, SysMenuManageService sysMenuManageService) {
		this.sysMenuService = sysMenuService;
		this.sysMenuManageService = sysMenuManageService;
	}

	@Operation(summary = "菜单扁平列表（全量，前端建树）")
	@PreAuthorize("@auth.decide('sysMenu:query')")
	@GetMapping("/list")
	public Result<List<SysMenuListVO>> list(SysMenuListQuery query) {
		return Result.success(sysMenuService.listFlat(query));
	}

	@Operation(summary = "菜单分页查询（表格视图）")
	@PreAuthorize("@auth.decide('sysMenu:query')")
	@GetMapping("/page")
	public Result<PageResponse<SysMenuListVO>> page(SysMenuPageQuery query) {
		Page<SysMenuEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<SysMenuListVO> result = sysMenuService.pageFlat(pageParams, query);
		return Result.success(result);
	}

	@Operation(summary = "菜单详情")
	@PreAuthorize("@auth.decide('sysMenu:query')")
	@GetMapping("/{id}")
	public Result<SysMenuDetailVO> detail(@PathVariable("id") Long id) {
		return Result.success(sysMenuService.getDetail(id));
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
		return Result.success(null, UPDATE_SUCCESS.getMessage());
	}

	@Operation(summary = "批量删除菜单（逻辑删除）")
	@PreAuthorize("@auth.decide('sysMenu:delete')")
	@DeleteMapping
	public Result<String> remove(@RequestBody List<String> ids) {
		List<Long> longIds = ids.stream().map(Long::valueOf).toList();
		sysMenuService.removeByIds(longIds);
		return Result.success(null, DELETE_SUCCESS.getMessage());
	}

	@Operation(summary = "批量更新菜单状态（0=启用 1=禁用）")
	@PreAuthorize("@auth.decide('sysMenu:update')")
	@PutMapping("/status")
	public Result<String> batchUpdateStatus(@Valid @RequestBody SysMenuBatchStatusForm form) {
		sysMenuManageService.batchUpdateStatus(form);
		return Result.success(null, UPDATE_SUCCESS.getMessage());
	}

	@Operation(summary = "查询菜单已分配角色")
	@PreAuthorize("@auth.decide('sysMenu:query')")
	@GetMapping("/{id}/roles")
	public Result<List<SysMenuAssignedRoleRow>> listRoles(@PathVariable("id") Long id) {
		return Result.success(sysMenuManageService.listAssignedRoles(id));
	}

	@Operation(summary = "覆盖分配菜单角色（写入 sys_menu_role）")
	@PreAuthorize("@auth.decide('sysMenu:update')")
	@PutMapping("/{id}/roles")
	public Result<String> replaceRoles(@PathVariable("id") Long id, @Valid @RequestBody SysMenuAssignRoleForm form) {
		sysMenuManageService.replaceMenuRoles(id, form);
		return Result.success(null, ASSIGN_SUCCESS.getMessage());
	}

	@Operation(summary = "查询权限列表并标记菜单是否已关联（sys_menu_permission）")
	@PreAuthorize("@auth.decide('sysMenu:query')")
	@GetMapping("/{id}/permissions")
	public Result<List<SysMenuAssignedPermissionRow>> listPermissions(@PathVariable("id") Long id) {
		return Result.success(sysMenuManageService.listPermissionsWithAssignFlags(id));
	}

	@Operation(summary = "覆盖分配菜单权限（写入 sys_menu_permission）")
	@PreAuthorize("@auth.decide('sysMenu:update')")
	@PutMapping("/{id}/permissions")
	public Result<String> replacePermissions(@PathVariable("id") Long id,
			@Valid @RequestBody SysMenuAssignPermissionForm form) {
		sysMenuManageService.replaceMenuPermissions(id, form);
		return Result.success(null, ASSIGN_SUCCESS.getMessage());
	}

}
