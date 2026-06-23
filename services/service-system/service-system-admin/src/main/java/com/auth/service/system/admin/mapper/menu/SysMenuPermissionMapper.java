package com.auth.service.system.admin.mapper.menu;

import com.auth.service.system.admin.model.entity.SysMenuPermissionEntity;
import com.auth.service.system.admin.model.po.menu.SysMenuPermissionLinkRowPO;
import com.auth.service.system.admin.model.po.permission.SysPermissionBoundMenuPO;
import com.auth.service.system.admin.model.po.reference.PermissionReferencePO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
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
	 * @param menuId 菜单ID
	 * @return 已分配权限 DTO 列表
	 */
	List<PermissionReferencePO> selectAssignedPermissionByMenuId(@Param("menuId") Long menuId);

	/**
	 * 批量查询菜单下已启用、未删除的按钮类权限关联
	 * @param menuIds 菜单 ID 列表
	 * @return 关联行
	 */
	List<SysMenuPermissionLinkRowPO> selectActiveButtonPermissionLinksByMenuIds(@Param("menuIds") List<Long> menuIds);

	/**
	 * 按权限 ID 反查已绑定菜单
	 * @param permissionId 权限 ID
	 * @return 已绑定菜单列表
	 */
	List<SysPermissionBoundMenuPO> selectBoundMenusByPermissionId(@Param("permissionId") Long permissionId);

	/**
	 * 按菜单 ID 反查已绑定权限（详情展示，不按权限类型过滤）
	 * @param menuId 菜单 ID
	 * @return 已绑定权限列表
	 */
	List<PermissionReferencePO> selectBoundPermissionsByMenuId(@Param("menuId") Long menuId);

	/**
	 * 删除菜单-权限关联
	 * @param menuId 菜单ID
	 */
	@Delete("DELETE FROM sys_menu_permission WHERE menu_id = #{menuId}")
	void deleteByMenuId(@Param("menuId") Long menuId);

	/**
	 * 批量插入菜单-权限关联
	 * @param menuId 菜单ID
	 * @param permissionIds 权限 ID 列表
	 * @return 插入行数
	 */
	int batchInsertMenuPermissions(@Param("menuId") Long menuId, @Param("permissionIds") List<Long> permissionIds);

}
