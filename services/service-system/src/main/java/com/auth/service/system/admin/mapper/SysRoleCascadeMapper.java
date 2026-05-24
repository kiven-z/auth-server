package com.auth.service.system.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色删除时关联表清理（不含 grant_table，删除前须无引用）。
 *
 * @author Bunny
 */
@Mapper
public interface SysRoleCascadeMapper {

	/**
	 * 删除菜单-角色关联。
	 * @param roleCode 角色编码
	 * @return 删除行数
	 */
	int deleteMenuRolesByRoleCode(@Param("roleCode") String roleCode);

	/**
	 * 角色编码变更时级联更新 grant_table。
	 * @param oldRoleCode 原角色编码
	 * @param newRoleCode 新角色编码
	 * @return 更新行数
	 */
	int updateGrantTableRoleCode(@Param("oldRoleCode") String oldRoleCode, @Param("newRoleCode") String newRoleCode);

	/**
	 * 角色编码变更时级联更新 sys_role_permission。
	 * @param oldRoleCode 原角色编码
	 * @param newRoleCode 新角色编码
	 * @return 更新行数
	 */
	int updateRolePermissionRoleCode(@Param("oldRoleCode") String oldRoleCode,
			@Param("newRoleCode") String newRoleCode);

	/**
	 * 角色编码变更时级联更新 sys_menu_role。
	 * @param oldRoleCode 原角色编码
	 * @param newRoleCode 新角色编码
	 * @return 更新行数
	 */
	int updateMenuRoleRoleCode(@Param("oldRoleCode") String oldRoleCode, @Param("newRoleCode") String newRoleCode);

}
