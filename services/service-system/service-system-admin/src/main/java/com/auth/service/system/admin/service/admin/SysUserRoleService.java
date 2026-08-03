package com.auth.service.system.admin.service.admin;

import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;

/**
 * 用户直连角色授权服务
 *
 * @author Bunny
 */
public interface SysUserRoleService {

	/**
	 * 全量覆盖用户直连角色授权
	 * @param userId 用户 ID
	 * @param form 角色编码列表
	 */
	void replaceUserRoles(Long userId, GrantTableAssignRoleForm form);

}
