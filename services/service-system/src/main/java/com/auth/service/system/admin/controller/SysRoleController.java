package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleCreateForm;
import com.auth.service.system.admin.model.query.SysRoleQuery;
import com.auth.service.system.admin.model.vo.role.SysRoleDetailVO;
import com.auth.service.system.admin.model.vo.role.SysRoleOptionVO;
import com.auth.service.system.admin.model.vo.role.SysRolePageResponse;
import com.auth.service.system.admin.service.SysRoleService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统角色管理 Controller
 *
 * @author Bunny
 */
@Tag(name = "系统角色管理")
@Slf4j
@RequestMapping("/api/system/role")
@RestController
public class SysRoleController {

	private final SysRoleService sysRoleService;

	public SysRoleController(SysRoleService sysRoleService) {
		this.sysRoleService = sysRoleService;
	}

	@Operation(summary = "角色分页查询")
	@PreAuthorize("@auth.decide('sysRole:query')")
	@GetMapping("/page")
	public Result<PageResponse<SysRolePageResponse>> page(SysRoleQuery query) {
		Page<SysRoleEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		PageResponse<SysRolePageResponse> result = sysRoleService.getPage(pageParams, query);
		return Result.success(result);
	}

	@Operation(summary = "角色详情")
	@PreAuthorize("@auth.decide('sysRole:detail')")
	@GetMapping("/{id}")
	public Result<SysRoleDetailVO> detail(@PathVariable("id") Long id) {
		SysRoleDetailVO vo = sysRoleService.getDetail(id);
		return Result.success(vo);
	}

	@Operation(summary = "新增角色")
	@PreAuthorize("@auth.decide('sysRole:create')")
	@PostMapping
	public Result<Void> create(@Valid @RequestBody SysRoleCreateForm form) {
		sysRoleService.create(form);
		return Result.success();
	}

	@Operation(summary = "角色下拉选项")
	@PreAuthorize("@auth.decide('sysRole:query')")
	@GetMapping("/options")
	public Result<List<SysRoleOptionVO>> options(@RequestParam(value = "roleName", required = false) String roleName,
			@RequestParam(value = "roleCode", required = false) String roleCode) {
		List<SysRoleOptionVO> list = sysRoleService.listOptions(roleName, roleCode);
		return Result.success(list);
	}

}
