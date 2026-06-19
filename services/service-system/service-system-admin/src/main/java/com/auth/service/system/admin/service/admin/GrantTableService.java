package com.auth.service.system.admin.service.admin;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.model.entity.GrantTableEntity;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * grant_table 读写与主体角色授权编排
 *
 * @author Bunny
 */
public interface GrantTableService extends IService<GrantTableEntity> {

	/**
	 * 查询主体已分配角色
	 * @param subjectType 授权主体类型
	 * @param subjectId 主体 ID
	 * @return 已分配角色行列表
	 */
	List<GrantTableAssignedRoleRow> listAssignedRoles(GrantTableSubjectType subjectType, Long subjectId);

	/**
	 * 按主体查询已绑定角色
	 * @param subjectType 授权主体类型
	 * @param subjectId 主体 ID
	 * @return 已分配角色行列表
	 */
	List<GrantTableAssignedRoleRow> listBoundRoles(GrantTableSubjectType subjectType, Long subjectId);

	/**
	 * 全量覆盖组织类主体（DEPT/POST）在 grant_table 中的角色授权
	 * @param subjectType 授权主体类型
	 * @param subjectId 主体 ID
	 * @param form 角色编码列表
	 */
	void replaceOrgSubjectRoles(GrantTableSubjectType subjectType, Long subjectId, GrantTableAssignRoleForm form);

	/**
	 * 全量覆盖指定主体的角色授权边（仅持久化，不含业务校验与失效触发）
	 * @param subjectType 主体类型
	 * @param subjectId 主体 ID
	 * @param roleCodes 角色编码列表
	 */
	void replaceSubjectRoleGrants(String subjectType, Long subjectId, List<String> roleCodes);

}
