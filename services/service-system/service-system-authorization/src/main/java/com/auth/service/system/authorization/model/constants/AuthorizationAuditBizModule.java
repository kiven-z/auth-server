package com.auth.service.system.authorization.model.constants;

import lombok.experimental.UtilityClass;

/**
 * authorization / admin 域操作审计「小模块」编码常量。
 * <p>
 * admin 侧组织与权限相关编码放此模块，避免 authorization 反向依赖 admin。
 *
 * @author Bunny
 */
@UtilityClass
public class AuthorizationAuditBizModule {

	/**
	 * 部门
	 */
	public static final String SYS_DEPT = "SYS_DEPT";

	/**
	 * 岗位
	 */
	public static final String SYS_POST = "SYS_POST";

	/**
	 * 用户
	 */
	public static final String SYS_USER = "SYS_USER";

	/**
	 * 角色
	 */
	public static final String SYS_ROLE = "SYS_ROLE";

	/**
	 * 菜单
	 */
	public static final String SYS_MENU = "SYS_MENU";

	/**
	 * 菜单角色
	 */
	public static final String SYS_MENU_ROLE = "SYS_MENU_ROLE";

	/**
	 * 权限
	 */
	public static final String SYS_PERMISSION = "SYS_PERMISSION";

	/**
	 * 角色权限
	 */
	public static final String SYS_ROLE_PERMISSION = "SYS_ROLE_PERMISSION";

	/**
	 * 部门闭包
	 */
	public static final String DEPT_CLOSURE = "DEPT_CLOSURE";

	/**
	 * 授权主体
	 */
	public static final String GRANT_TABLE = "GRANT_TABLE";

	/**
	 * 授权失效 Outbox 投递队列
	 */
	public static final String AUTH_INVALIDATION_OUTBOX = "AUTH_INVALIDATION_OUTBOX";

	/**
	 * 授权失效幂等事件
	 */
	public static final String AUTH_INVALIDATION_EVENT = "AUTH_INVALIDATION_EVENT";

}
