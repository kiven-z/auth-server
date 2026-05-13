package com.auth.service.auth.internal.authorization;

/**
 * 授权变更维度（文档/后续统一失效接口用）：与影响面反查入口对应关系大致如下
 * <ul>
 * <li>{@link #ROLE} — sys_role、role_scope、批量 roleCode</li>
 * <li>{@link #PERMISSION} — sys_permission、sys_role_permission、permissionCodes</li>
 * <li>{@link #POLICY} — role_policy、policy_definition、policyCodes</li>
 * <li>{@link #GRANT} — grant_table、GrantSubject 列表</li>
 * <li>{@link #USER_DEPT} — user_dept、部门成员 deptIds</li>
 * <li>{@link #USER_POST} — user_post、岗位人员 postIds</li>
 * <li>{@link #USER_SCOPE} — user_scope（通常直指 userId，可单独扩展）</li>
 * </ul>
 *
 * @author Bunny
 */
public enum AuthorizationChangeKind {

	/**
	 * 角色相关
	 */
	ROLE,

	/**
	 * 权限码相关
	 */
	PERMISSION,

	/**
	 * 策略相关
	 */
	POLICY,

	/**
	 * grant_table 授权边
	 */
	GRANT,

	/**
	 * 用户-部门关系
	 */
	USER_DEPT,

	/**
	 * 用户-岗位关系
	 */
	USER_POST,

	/**
	 * 用户级数据范围覆盖
	 */
	USER_SCOPE

}
