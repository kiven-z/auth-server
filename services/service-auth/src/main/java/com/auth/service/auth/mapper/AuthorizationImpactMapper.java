package com.auth.service.auth.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 授权影响面：按角色/权限/策略/grant 关联维度反查受影响用户 ID（与 UserRolesCte 同源）。
 *
 * @author Bunny
 */
@Mapper
public interface AuthorizationImpactMapper {

	/**
	 * 按单个角色编码展开用户 ID（USER/DEPT/POST grant）
	 * @param roleCode 角色编码
	 * @return 用户 ID 列表
	 */
	List<Long> selectUserIdsByRoleCode(@Param("roleCode") String roleCode);

	/**
	 * 按多个角色编码展开用户 ID
	 * @param roleCodes 角色编码列表
	 * @return 用户 ID 列表
	 */
	List<Long> selectUserIdsByRoleCodes(@Param("roleCodes") List<String> roleCodes);

	/**
	 * 按多个权限码（启用状态）经角色-grant 展开用户 ID
	 * @param permissionCodes 权限码列表
	 * @return 用户 ID 列表
	 */
	List<Long> selectUserIdsByPermissionCodes(@Param("permissionCodes") List<String> permissionCodes);

	/**
	 * 按多个策略码（role_policy 未删除）经角色-grant 展开用户 ID
	 * @param policyCodes 策略码列表
	 * @return 用户 ID 列表
	 */
	List<Long> selectUserIdsByPolicyCodes(@Param("policyCodes") List<String> policyCodes);

	/**
	 * 部门成员（user_dept.status=0）
	 * @param deptIds 部门 ID 列表
	 * @return 用户 ID 列表
	 */
	List<Long> selectUserIdsByDeptIds(@Param("deptIds") List<Long> deptIds);

	/**
	 * 岗位人员（user_post 状态与画像一致）
	 * @param postIds 岗位 ID 列表
	 * @return 用户 ID 列表
	 */
	List<Long> selectUserIdsByPostIds(@Param("postIds") List<Long> postIds);

}
