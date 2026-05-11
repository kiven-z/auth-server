package com.auth.service.system.admin.mapper;

import com.auth.service.system.admin.model.dto.menu.SysMenuAssignedPermissionDTO;
import com.auth.service.system.admin.model.entity.SysMenuPermissionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单权限关联 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysMenuPermissionMapper extends BaseMapper<SysMenuPermissionEntity> {

    /**
     * 查询菜单已分配权限（联表获取权限名称）
     *
     * @param menuId 菜单ID
     * @return 已分配权限 DTO 列表
     */
    List<SysMenuAssignedPermissionDTO> selectAssignedPermissionByMenuId(@Param("menuId") Long menuId);

    /**
     * 删除菜单-权限关联
     *
     * @param menuId 菜单ID
     * @return 删除行数
     */
    int deleteByMenuId(@Param("menuId") Long menuId);

    /**
     * 批量插入菜单-权限关联
     *
     * @param menuId          菜单ID
     * @param permissionCodes 权限编码列表
     * @return 插入行数
     */
    int batchInsertMenuPermissions(@Param("menuId") Long menuId, @Param("permissionCodes") List<String> permissionCodes);

}
