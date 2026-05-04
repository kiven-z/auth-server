package com.auth.service.system.admin.role.controller;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.role.model.entity.SysRoleEntity;
import com.auth.service.system.admin.role.model.form.SysRoleCreateForm;
import com.auth.service.system.admin.role.model.form.SysRoleStatusForm;
import com.auth.service.system.admin.role.model.form.SysRoleUpdateForm;
import com.auth.service.system.admin.role.model.query.SysRoleQuery;
import com.auth.service.system.admin.role.model.vo.SysRoleDetailVO;
import com.auth.service.system.admin.role.model.vo.SysRoleOptionVO;
import com.auth.service.system.admin.role.model.vo.SysRolePageResponse;
import com.auth.service.system.admin.role.service.SysRoleService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.SystemResultCodeEnum;
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

    @Operation(summary = "修改角色")
    @PreAuthorize("@auth.decide('sysRole:update')")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody SysRoleUpdateForm form) {
        sysRoleService.update(form);
        return Result.success();
    }

    @Operation(summary = "批量删除角色")
    @PreAuthorize("@auth.decide('sysRole:delete')")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw new SystemBusinessException(SystemResultCodeEnum.REQUEST_IS_EMPTY);
        }
        sysRoleService.deleteBatch(ids);
        return Result.success();
    }

    @Operation(summary = "切换角色状态")
    @PreAuthorize("@auth.decide('sysRole:update')")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable("id") Long id, @Valid @RequestBody SysRoleStatusForm form) {
        sysRoleService.updateStatus(id, form.getStatus());
        return Result.success();
    }

    @Operation(summary = "角色下拉选项")
    @PreAuthorize("@auth.decide('sysRole:query')")
    @GetMapping("/options")
    public Result<List<SysRoleOptionVO>> options(
            @RequestParam(value = "roleName", required = false) String roleName,
            @RequestParam(value = "roleCode", required = false) String roleCode) {
        List<SysRoleOptionVO> list = sysRoleService.listOptions(roleName, roleCode);
        return Result.success(list);
    }

}
