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

}
