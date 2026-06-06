package com.auth.service.system.admin.mapper;

import com.auth.service.system.admin.model.po.granttable.GrantTableBoundRolePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * grant_table 读写
 *
 * @author Bunny
 */
@Mapper
public interface GrantTableMapper {

	/**
	 * 统计指定角色码在 grant_table 中的引用行数。
	 * @param roleCode 角色编码
	 * @return 引用行数
	 */
	long countByRoleCode(@Param("roleCode") String roleCode);

	/**
	 * 按授权主体删除全部 grant 行。
	 * @param subjectType 主体类型
	 * @param subjectId 主体 ID
	 * @return 删除行数
	 */
	int deleteBySubject(@Param("subjectType") String subjectType, @Param("subjectId") Long subjectId);

	/**
	 * 批量插入主体-角色授权边。
	 * @param subjectType 主体类型
	 * @param subjectId 主体 ID
	 * @param roleCodes 角色编码列表
	 * @param operatorId 操作人 ID
	 * @return 插入行数
	 */
	int batchInsertSubjectRoleGrants(@Param("subjectType") String subjectType, @Param("subjectId") Long subjectId,
			@Param("roleCodes") List<String> roleCodes, @Param("operatorId") Long operatorId);

	/**
	 * 按授权主体查询已绑定角色（联表 sys_role）。
	 * @param subjectType 主体类型
	 * @param subjectId 主体 ID
	 * @param roleTypes 允许展示的 role_type 集合
	 * @return 已授权角色投影列表
	 */
	List<GrantTableBoundRolePO> selectBoundRolesBySubject(@Param("subjectType") String subjectType,
			@Param("subjectId") Long subjectId, @Param("roleTypes") List<Integer> roleTypes);

}
