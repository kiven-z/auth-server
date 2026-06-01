package com.auth.service.system.admin.mapper;

import com.auth.service.system.admin.model.entity.SysRolePermissionEntity;
import com.auth.service.system.admin.model.po.permission.SysPermissionBoundRolePO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
	 * 按角色编码删除全部权限关联
	 * @param roleCode 角色编码
	 * @return 删除行数
	 */
	int deleteByRoleCode(@Param("roleCode") String roleCode);

	/**
	 * 查询角色已分配的权限码列表
	 * @param roleCode 角色编码
	 * @return 权限码列表
	 */
	List<String> selectPermissionCodesByRoleCode(@Param("roleCode") String roleCode);

	/**
	 * 按权限码反查已绑定角色
	 * @param permissionCode 权限编码
	 * @return 已绑定角色列表
	 */
	List<SysPermissionBoundRolePO> selectBoundRolesByPermissionCode(@Param("permissionCode") String permissionCode);

	/**
	 * 批量插入角色权限关联
	 * @param roleCode 角色编码
	 * @param permissionCodes 权限码列表
	 * @param grantorId 授权人 ID
	 * @param createUser 创建人 ID
	 * @return 插入行数
	 */
	int batchInsertRolePermissions(@Param("roleCode") String roleCode,
			@Param("permissionCodes") List<String> permissionCodes, @Param("grantorId") Long grantorId,
			@Param("createUser") Long createUser);

}
