package com.auth.service.system.admin.role.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.role.model.entity.SysRoleEntity;
import com.auth.service.system.admin.role.model.query.SysRoleQuery;
import com.auth.service.system.admin.role.model.vo.SysRolePageResponse;
import com.auth.service.system.admin.role.service.SysRoleService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 系统角色接口
 *
 * @author Bunny
 */
@Tag(name = "系统角色", description = "角色管理")
@RequestMapping("/api/system/v1/role")
@RestController
public class SysRoleController {

	private final SysRoleService sysRoleService;

	public SysRoleController(SysRoleService sysRoleService) {
		this.sysRoleService = sysRoleService;
	}

	/**
	 * 分页查询角色
	 * @param query 查询与分页参数
	 * @return 分页列表
	 */
	@Operation(summary = "分页查询角色")
	@PreAuthorize("@auth.decide('sysRole:query')")
	@GetMapping("page")
	public Result<PageResponse<SysRolePageResponse>> page(SysRoleQuery query) {
		Page<SysRoleEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<SysRolePageResponse> response = sysRoleService.getPage(pageParams, query);
		return Result.success(response);
	}

}
