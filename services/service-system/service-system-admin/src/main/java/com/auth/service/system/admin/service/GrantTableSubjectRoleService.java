package com.auth.service.system.admin.service;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;

import java.util.List;

/**
 * grant_table 主体角色授权（查询与全量覆盖）
 *
 * @author Bunny
 */
public interface GrantTableSubjectRoleService {

	/**
	 * 查询主体已分配角色。
	 * @param subjectType 授权主体类型
	 * @param subjectId 主体 ID
	 * @return 已分配角色行列表
	 */
	List<GrantTableAssignedRoleRow> listAssignedRoles(GrantTableSubjectType subjectType, Long subjectId);

	/**
	 * 全量覆盖主体在 grant_table 中的角色授权。
	 * @param subjectType 授权主体类型
	 * @param subjectId 主体 ID
	 * @param form 角色编码列表
	 */
	void replaceSubjectRoles(GrantTableSubjectType subjectType, Long subjectId, GrantTableAssignRoleForm form);

}
