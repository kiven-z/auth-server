package com.auth.service.system.admin.mapper.role;

import com.auth.service.system.admin.model.entity.SysRolePermissionEntity;
import com.auth.service.system.admin.model.po.permission.SysPermissionBoundRolePO;
import com.auth.service.system.admin.model.po.role.SysRoleAssignedPermissionPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色权限关联 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermissionEntity> {

	/**
	 * 按角色 ID 删除全部权限关联
	 * @param roleId 角色 ID
	 */
	@Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
	void deleteByRoleId(@Param("roleId") Long roleId);

	/**
	 * 查询角色已分配权限
	 * @param roleId 角色 ID
	 * @return 已分配权限列表
	 */
	List<SysRoleAssignedPermissionPO> selectAssignedPermissionsByRoleId(@Param("roleId") Long roleId);

	/**
	 * 按权限 ID 反查已绑定角色
	 * @param permissionId 权限 ID
	 * @return 已绑定角色列表
	 */
	List<SysPermissionBoundRolePO> selectBoundRolesByPermissionId(@Param("permissionId") Long permissionId);

	/**
	 * 批量插入角色权限关联
	 * @param roleId 角色 ID
	 * @param permissionIds 权限 ID 列表
	 * @param grantorId 授权人 ID
	 * @param createUser 创建人 ID
	 * @return 插入行数
	 */
	int batchInsertRolePermissions(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds,
			@Param("grantorId") Long grantorId, @Param("createUser") Long createUser);

}
