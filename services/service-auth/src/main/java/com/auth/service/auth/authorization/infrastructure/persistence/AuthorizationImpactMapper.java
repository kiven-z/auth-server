package com.auth.service.auth.authorization.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 授权影响面
 *
 * @author Bunny
 */
@Mapper
public interface AuthorizationImpactMapper {

	/**
	 * 按角色码反查可能持有该角色的用户 ID
	 * @param roleCodes 角色编码列表，非空
	 * @return 用户 ID 列表（可能含重复，由上层去重）
	 */
	List<Long> selectUserIdsByRoleCodes(@Param("roleCodes") List<String> roleCodes);

	/**
	 * 按 grant_table USER 主体 ID 反查用户 ID。
	 * @param subjectIds USER 主体 ID 列表，非空
	 * @return 用户 ID 列表
	 */
	List<Long> selectUserIdsByGrantUserSubjectIds(@Param("subjectIds") List<Long> subjectIds);

	/**
	 * 按 grant_table DEPT 主体 ID 反查用户 ID。
	 * @param subjectIds DEPT 主体 ID 列表，非空
	 * @return 用户 ID 列表
	 */
	List<Long> selectUserIdsByGrantDeptSubjectIds(@Param("subjectIds") List<Long> subjectIds);

	/**
	 * 按 grant_table POST 主体 ID 反查用户 ID。
	 * @param subjectIds POST 主体 ID 列表，非空
	 * @return 用户 ID 列表
	 */
	List<Long> selectUserIdsByGrantPostSubjectIds(@Param("subjectIds") List<Long> subjectIds);

	/**
	 * 按部门 ID 反查成员用户 ID（含子部门）。
	 * @param deptIds 部门 ID 列表，非空
	 * @return 用户 ID 列表
	 */
	List<Long> selectUserIdsByDeptIds(@Param("deptIds") List<Long> deptIds);

	/**
	 * 按岗位 ID 反查成员用户 ID。
	 * @param postIds 岗位 ID 列表，非空
	 * @return 用户 ID 列表
	 */
	List<Long> selectUserIdsByPostIds(@Param("postIds") List<Long> postIds);

	/**
	 * 按权限码桥接查询关联角色码。
	 * @param permissionCodes 权限码列表，非空
	 * @return 角色码列表
	 */
	List<String> selectRoleCodesByPermissionCodes(@Param("permissionCodes") List<String> permissionCodes);

	/**
	 * 按策略码桥接查询关联角色码。
	 * @param policyCodes 策略码列表，非空
	 * @return 角色码列表
	 */
	List<String> selectRoleCodesByPolicyCodes(@Param("policyCodes") List<String> policyCodes);

	/**
	 * 按策略码查询直连绑定的用户 ID。
	 * @param policyCodes 策略码列表，非空
	 * @return 用户 ID 列表
	 */
	List<Long> selectUserIdsByPolicyBinding(@Param("policyCodes") List<String> policyCodes);

}
