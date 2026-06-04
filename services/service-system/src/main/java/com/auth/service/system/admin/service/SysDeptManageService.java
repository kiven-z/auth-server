package com.auth.service.system.admin.service;

import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;

import java.util.List;

/**
 * 部门扩展管理
 *
 * @author bunny
 */
public interface SysDeptManageService {

	/**
	 * 查询部门已分配角色 仅 roleType=3/4
	 * @param deptId 部门 ID
	 * @return 已分配角色行
	 */
	List<GrantTableAssignedRoleRow> listAssignedRoles(Long deptId);

	/**
	 * 全量覆盖部门角色授权。
	 * @param deptId 部门 ID
	 * @param form 角色编码列表
	 */
	void replaceDeptRoles(Long deptId, GrantTableAssignRoleForm form);

}
