package com.auth.service.system.admin.mapper;

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
	 * 统计指定授权主体在 grant_table 中的引用行数。
	 * @param subjectType 主体类型
	 * @param subjectId 主体 ID
	 * @return 引用行数
	 */
	long countBySubject(@Param("subjectType") String subjectType, @Param("subjectId") Long subjectId);

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

}
