package com.auth.service.system.admin.service;

import com.auth.service.system.admin.model.vo.role.SysRoleAssignedPermissionRow;

import java.util.List;

/**
 * 角色权限分配服务。
 *
 * @author Bunny
 */
public interface SysRolePermissionService {

	/**
	 * 查询角色已分配权限
	 * @param roleCode 角色编码
	 * @return 已分配权限行列表
	 */
	List<SysRoleAssignedPermissionRow> listAssignedPermissions(String roleCode);

	/**
	 * 全量替换角色权限码并触发授权失效。
	 * @param roleCode 角色编码
	 * @param permissionCodes 权限码列表（可为空表示清空）
	 */
	void assignPermissions(String roleCode, List<String> permissionCodes);

}
