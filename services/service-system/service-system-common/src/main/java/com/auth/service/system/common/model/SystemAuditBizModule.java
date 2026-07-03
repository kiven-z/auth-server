package com.auth.service.system.common.model;

import lombok.experimental.UtilityClass;

/**
 * system 服务操作审计「小模块」编码常量
 *
 * @author Bunny
 */
@UtilityClass
public class SystemAuditBizModule {

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
	 * 文件
	 */
	public static final String SYS_FILE = "SYS_FILE";

	/**
	 * 邮件模板
	 */
	public static final String SYS_EMAIL_TEMPLATE = "SYS_EMAIL_TEMPLATE";

	/**
	 * 定时任务
	 */
	public static final String SYS_JOB = "SYS_JOB";

	/**
	 * 定时任务分组
	 */
	public static final String SYS_JOB_GROUP = "SYS_JOB_GROUP";

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
