package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignPermissionForm;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignRoleForm;
import com.auth.service.system.admin.model.vo.menu.SysMenuAssignedPermissionRow;
import com.auth.service.system.admin.model.vo.menu.SysMenuAssignedRoleRow;
import com.auth.service.system.admin.service.SysMenuManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.SystemResultCodeEnum.ASSIGN_SUCCESS;

/**
 * 系统菜单管理
 *
 * @author Bunny
 */
@Tag(name = "系统菜单管理")
@Slf4j
@RequestMapping("/api/system/menu")
@RestController
public class SysMenuManageController {

    private final SysMenuManageService sysMenuManageService;

    public SysMenuManageController(SysMenuManageService sysMenuManageService) {
        this.sysMenuManageService = sysMenuManageService;
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
