package com.auth.service.system.admin.service.impl;

import com.auth.service.system.admin.mapper.SysMenuMapper;
import com.auth.service.system.admin.mapper.SysMenuPermissionMapper;
import com.auth.service.system.admin.mapper.SysMenuRoleMapper;
import com.auth.service.system.admin.model.dto.menu.SysMenuAssignedPermissionDTO;
import com.auth.service.system.admin.model.dto.menu.SysMenuAssignedRoleDTO;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignPermissionForm;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignRoleForm;
import com.auth.service.system.admin.model.vo.menu.SysMenuAssignedPermissionRow;
import com.auth.service.system.admin.model.vo.menu.SysMenuAssignedRoleRow;
import com.auth.service.system.admin.service.SysMenuManageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 菜单管理实现
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysMenuManageServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuEntity>
        implements SysMenuManageService {

    private final SysMenuRoleMapper sysMenuRoleMapper;

    private final SysMenuPermissionMapper sysMenuPermissionMapper;

    public SysMenuManageServiceImpl(SysMenuRoleMapper sysMenuRoleMapper,
                                    SysMenuPermissionMapper sysMenuPermissionMapper) {
        this.sysMenuRoleMapper = sysMenuRoleMapper;
        this.sysMenuPermissionMapper = sysMenuPermissionMapper;
    }

    /**
     * 当前菜单已分配角色（供前端与角色分页合并展示）
     *
     * @param menuId 菜单主键
     * @return 已分配行，{@code assigned} 恒为 true
     */
    @Override
    public List<SysMenuAssignedRoleRow> listAssignedRoles(Long menuId) {
        List<SysMenuAssignedRoleDTO> dtoList = sysMenuRoleMapper.selectAssignedRolesByMenuId(menuId);

        return dtoList.stream().map(dto -> {
            String roleCode = dto.getRoleCode();
            String roleName = dto.getRoleName();
            return new SysMenuAssignedRoleRow(roleCode, roleName, true);
        }).toList();
    }

    /**
     * 覆盖写入菜单-角色关联
     *
     * @param menuId 菜单主键
     * @param form   角色编码列表
     */
    @Override
    public void replaceMenuRoles(Long menuId, SysMenuAssignRoleForm form) {
        List<String> roleCodes = form.getRoleCodes();
        sysMenuRoleMapper.deleteByMenuId(menuId);
        sysMenuRoleMapper.batchInsertMenuRoles(menuId, roleCodes);

    }

    /**
     * 全量权限列表并标记当前菜单是否已关联（供分配抽屉）
     *
     * @param menuId 菜单主键
     * @return 权限行
     */
    @Override
    public List<SysMenuAssignedPermissionRow> listPermissionsWithAssignFlags(Long menuId) {
        List<SysMenuAssignedPermissionDTO> dtoList = sysMenuPermissionMapper.selectAssignedPermissionByMenuId(menuId);

        return dtoList.stream().map(dto -> {
            String permissionCode = dto.getPermissionCode();
            String permissionName = dto.getPermissionName();
            return new SysMenuAssignedPermissionRow(permissionCode, permissionName, true);
        }).toList();
    }

    /**
     * 覆盖写入菜单-权限关联
     *
     * @param menuId 菜单主键
     * @param form   权限编码列表
     */
    @Override
    public void replaceMenuPermissions(Long menuId, SysMenuAssignPermissionForm form) {
        List<String> permissionCodes = form.getPermissionCodes();
        sysMenuPermissionMapper.deleteByMenuId(menuId);
        sysMenuPermissionMapper.batchInsertMenuPermissions(menuId, permissionCodes);
    }
}
