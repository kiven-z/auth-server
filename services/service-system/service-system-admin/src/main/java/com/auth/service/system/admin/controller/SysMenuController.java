package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuBatchStatusForm;
import com.auth.service.system.admin.model.form.menu.SysMenuMoveForm;
import com.auth.service.system.admin.model.form.menu.SysMenuSaveForm;
import com.auth.service.system.admin.model.query.SysMenuQuery;
import com.auth.service.system.admin.model.vo.menu.RouteNodeVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuDetailVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuListVO;
import com.auth.service.system.admin.service.SysMenuService;
import com.auth.service.system.common.model.SystemAuditBizModule;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

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
	@Operation(summary = "动态路由列表")
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

	@OperationLog(targetType = "MENU", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_MENU, operation = OperationLogKind.CREATE)
	@Operation(summary = "新增菜单")
	@PreAuthorize("@auth.decide('sysMenu:create')")
	@PostMapping
	public Result<String> create(@Valid @RequestBody SysMenuSaveForm form) {
		String id = sysMenuService.create(form);
		return Result.success(id, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "MENU", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_MENU, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新菜单")
	@PreAuthorize("@auth.decide('sysMenu:update')")
	@PutMapping
	public Result<String> update(@Validated(UpdateGroup.class) @RequestBody SysMenuSaveForm form) {
		sysMenuService.update(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "MENU", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_MENU, operation = OperationLogKind.DELETE)
	@Operation(summary = "批量删除菜单")
	@PreAuthorize("@auth.decide('sysMenu:delete')")
	@DeleteMapping
	public Result<String> remove(@RequestBody List<Long> ids) {
		sysMenuService.deleteByIds(ids);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "MENU", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_MENU, operation = OperationLogKind.UPDATE)
	@Operation(summary = "移动菜单")
	@PreAuthorize("@auth.decide('sysMenu:update')")
	@PutMapping("/{id}/move")
	public Result<String> move(@PathVariable("id") Long id, @Valid @RequestBody SysMenuMoveForm form) {
		sysMenuService.move(id, form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "MENU", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_MENU, operation = OperationLogKind.UPDATE)
	@Operation(summary = "批量更新菜单状态")
	@PreAuthorize("@auth.decide('sysMenu:update')")
	@PutMapping("/status")
	public Result<String> batchUpdateStatus(@Valid @RequestBody SysMenuBatchStatusForm form) {
		sysMenuService.batchUpdateStatus(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
