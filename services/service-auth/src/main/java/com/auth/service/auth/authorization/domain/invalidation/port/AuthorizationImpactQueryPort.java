package com.auth.service.auth.authorization.domain.invalidation.port;

import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;

import java.util.Collection;
import java.util.Set;

/**
 * 授权影响面查询端口
 *
 * @author Bunny
 */
public interface AuthorizationImpactQueryPort {

	/**
	 * 按角色码反查所有可能持有该角色的用户 ID
	 * @param roleCodes 角色编码，非空
	 * @return 去重后的用户 ID 集合
	 */
	Set<Long> findUserIdsByRoleCodes(Collection<String> roleCodes);

	/**
	 * 按 grant_table 授权主体反查受影响用户 ID
	 * @param subjects 授权主体键列表，非空
	 * @return 去重后的用户 ID 集合
	 */
	Set<Long> findUserIdsByGrantSubjects(Collection<GrantSubjectKey> subjects);

	/**
	 * 按部门 ID 反查成员用户 ID（含子部门）
	 * @param deptIds 部门 ID，非空
	 * @return 去重后的用户 ID 集合
	 */
	Set<Long> findUserIdsByDeptIds(Collection<Long> deptIds);

	/**
	 * 按岗位 ID 反查成员用户 ID。
	 * @param postIds 岗位 ID，非空
	 * @return 去重后的用户 ID 集合
	 */
	Set<Long> findUserIdsByPostIds(Collection<Long> postIds);

	/**
	 * 按权限码反查受影响用户 ID（桥接角色码后走 grant 反向）。
	 * @param permissionCodes 权限码，非空
	 * @return 去重后的用户 ID 集合
	 */
	Set<Long> findUserIdsByPermissionCodes(Collection<String> permissionCodes);

	/**
	 * 按策略码反查受影响用户 ID（角色桥接 + 用户直连绑定）。
	 * @param policyCodes 策略码，非空
	 * @return 去重后的用户 ID 集合
	 */
	Set<Long> findUserIdsByPolicyCodes(Collection<String> policyCodes);

}
