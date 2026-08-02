package com.auth.service.system.admin.service.admin;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.form.role.SysRoleGrantSubjectsForm;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;
import com.auth.service.system.admin.model.vo.role.SysRoleGrantSubjectsVO;

import java.util.List;

/**
 * grant_table 读写与主体角色授权编排
 *
 * @author Bunny
 */
public interface GrantTableService {

	/**
	 * 查询主体已分配角色
	 * @param subjectType 主体类型
	 * @param subjectId 主体 ID
	 * @return 已分配角色回显列表
	 */
	List<RoleReferenceVO> listAssignedRoles(GrantTableSubjectType subjectType, Long subjectId);

	/**
	 * 查询主体已绑定角色
	 * @param subjectType 主体类型
	 * @param subjectId 主体 ID
	 * @return 已绑定角色回显列表
	 */
	List<RoleReferenceVO> listBoundRoles(GrantTableSubjectType subjectType, Long subjectId);

	/**
	 * 按角色与主体类型查询授权对象
	 * @param roleId 角色 ID
	 * @param subjectType 主体类型（USER / DEPT / POST）
	 * @return 仅含该类型种子项；无授权时 subjects 为空列表
	 */
	SysRoleGrantSubjectsVO listGrantSubjectsByRoleId(Long roleId, GrantTableSubjectType subjectType);

	/**
	 * 按角色与主体类型全量覆盖授权对象（仅替换 form 指定类型）
	 * @param roleId 角色 ID
	 * @param form 主体类型与主体 ID 列表
	 */
	void replaceGrantSubjectsByRoleId(Long roleId, SysRoleGrantSubjectsForm form);

	/**
	 * 全量覆盖组织主体角色授权
	 * @param subjectType 主体类型（DEPT/POST）
	 * @param subjectId 主体 ID
	 * @param form 角色 ID 列表
	 */
	void replaceOrgSubjectRoles(GrantTableSubjectType subjectType, Long subjectId, GrantTableAssignRoleForm form);

	/**
	 * 全量覆盖主体角色授权
	 * @param subjectType 主体类型字符串
	 * @param subjectId 主体 ID
	 * @param roleIds 角色 ID 列表
	 */
	void replaceSubjectRoleGrants(String subjectType, Long subjectId, List<Long> roleIds);

}
