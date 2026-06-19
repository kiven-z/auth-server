package com.auth.service.system.admin.service.admin;

import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;

import java.util.List;

/**
 * 用户直连角色授权（grant_table）
 *
 * @author Bunny
 */
public interface SysUserRoleService {

	/**
	 * 查询用户已分配角色
	 * @param userId 用户 ID
	 * @return 已分配角色行列表
	 */
	List<GrantTableAssignedRoleRow> listAssignedRoles(Long userId);

	/**
	 * 全量覆盖用户在 grant_table 中的直连角色授权
	 * @param userId 用户 ID
	 * @param form 角色编码列表
	 */
	void replaceUserRoles(Long userId, GrantTableAssignRoleForm form);

}
