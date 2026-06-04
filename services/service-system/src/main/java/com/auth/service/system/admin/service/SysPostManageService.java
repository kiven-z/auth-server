package com.auth.service.system.admin.service;

import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;

import java.util.List;

/**
 * 岗位扩展管理
 *
 * @author Bunny
 */
public interface SysPostManageService {

	/**
	 * 查询岗位已分配角色，仅 roleType=3/4
	 * @param postId 岗位 ID
	 * @return 已分配角色行
	 */
	List<GrantTableAssignedRoleRow> listAssignedRoles(Long postId);

	/**
	 * 全量覆盖岗位角色授权
	 * @param postId 岗位 ID
	 * @param form 角色编码列表
	 */
	void replacePostRoles(Long postId, GrantTableAssignRoleForm form);

}
