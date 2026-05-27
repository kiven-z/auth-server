package com.auth.service.system.admin.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleCreateForm;
import com.auth.service.system.admin.model.form.role.SysRoleUpdateForm;
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

	@Operation(summary = "更新角色")
	@PreAuthorize("@auth.decide('sysRole:update')")
	@PutMapping("/{id}")
	public Result<Void> update(@PathVariable("id") Long id,
			@Validated(UpdateGroup.class) @RequestBody SysRoleUpdateForm form) {
		form.setId(id);
		sysRoleService.update(form);
		return Result.success();
	}

	@Operation(summary = "批量删除角色")
	@PreAuthorize("@auth.decide('sysRole:delete')")
	@DeleteMapping
	public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
		sysRoleService.removeBatchByIds(ids);
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
