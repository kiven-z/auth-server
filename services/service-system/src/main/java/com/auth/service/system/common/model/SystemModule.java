package com.auth.service.system.common.model;

import lombok.Getter;

import static com.auth.service.system.common.model.SystemModule.SystemModuleConstant.*;

/**
 * 系统模块枚举。
 * <p>
 * 枚举 name 为模块标识（如 SYS_USER），type 为模块分类，description 为中文展示名称。 用于前端远程搜索下拉、权限配置等场景。
 * </p>
 *
 * @author Bunny
 */
@Getter
public enum SystemModule {

	// ========== 系统业务（SYS） ==========
	/**
	 * 部门
	 */
	SYS_DEPT(SYS, "部门"),
	/**
	 * 岗位
	 */
	SYS_POST(SYS, "岗位"),
	/**
	 * 用户
	 */
	SYS_USER(SYS, "用户"),
	/**
	 * 角色
	 */
	SYS_ROLE(SYS, "角色"),
	/**
	 * 菜单
	 */
	SYS_MENU(SYS, "菜单"),
	/**
	 * 菜单权限
	 */
	SYS_MENU_PERMISSION(SYS, "菜单权限"),
	/**
	 * 菜单角色
	 */
	SYS_MENU_ROLE(SYS, "菜单角色"),
	/**
	 * 权限
	 */
	SYS_PERMISSION(SYS, "权限"),
	/**
	 * 角色权限
	 */
	SYS_ROLE_PERMISSION(SYS, "角色权限"),
	/**
	 * 部门闭包
	 */
	DEPT_CLOSURE(SYS, "部门闭包"),
	/**
	 * 邮件模板
	 */
	SYS_EMAIL_TEMPLATE(SYS, "邮件模板"),
	/**
	 * 定时任务
	 */
	SYS_JOB(SYS, "定时任务"),
	/**
	 * 定时任务分组
	 */
	SYS_JOB_GROUP(SYS, "定时任务分组"),
	/**
	 * 文件
	 */
	SYS_FILES(SYS, "文件"),
	/**
	 * 文件分片详情
	 */
	SYS_FILES_PART_DETAIL(SYS, "文件分片详情"),

	// ========== 用户相关（USER） ==========

	/**
	 * 用户部门关联
	 */
	USER_DEPT(USER, "用户部门关联"),
	/**
	 * 用户岗位关联
	 */
	USER_POST(USER, "用户岗位关联"),

	// ========== 权限/授权（GRANT） ==========

	/**
	 * 授权主体
	 */
	GRANT_TABLE(GRANT, "授权主体"),
	/**
	 * 数据范围授权
	 */
	DATA_SCOPE_GRANT_TABLE(GRANT, "数据范围授权"),

	// ========== 访问策略（POLICY） ==========

	/**
	 * 访问策略绑定
	 */
	USER_ACCESS_POLICY_BINDING(POLICY, "访问策略绑定"),
	/**
	 * 访问策略定义
	 */
	USER_ACCESS_POLICY_DEFINITION(POLICY, "访问策略定义"),
	/**
	 * 访问策略维度
	 */
	USER_ACCESS_POLICY_DIMENSION(POLICY, "访问策略维度"),

	// ========== 审计日志（AUDIT） ==========

	/**
	 * 授权审计日志
	 */
	LOG_AUTHORIZATION_AUDIT(AUDIT, "授权审计日志"),
	/**
	 * 数据范围审计日志
	 */
	LOG_DATA_SCOPE_AUDIT(AUDIT, "数据范围审计日志"),
	/**
	 * 策略执行日志
	 */
	LOG_POLICY_EXECUTION(AUDIT, "策略执行日志"),
	/**
	 * 密码历史
	 */
	LOG_USER_PASSWORD_HISTORY(USER, "密码历史日志"),

	// ========== 操作日志（LOG） ==========

	/**
	 * 操作日志
	 */
	LOG_OPERATION(LOG, "操作日志"),
	/**
	 * 登录日志
	 */
	LOG_LOGIN(LOG, "登录日志"),
	/**
	 * 任务调度日志
	 */
	LOG_JOB(LOG, "任务调度日志"),;

	/**
	 * 模块分类：SYS-系统业务、USER-用户相关、GRANT-授权、POLICY-策略、AUDIT-审计日志、LOG-操作日志
	 */
	private final String type;

	/**
	 * 模块中文描述
	 */
	private final String description;

	SystemModule(String type, String description) {
		this.type = type;
		this.description = description;
	}

	static class SystemModuleConstant {

		protected static final String SYS = "SYS";

		protected static final String USER = "USER";

		protected static final String GRANT = "GRANT";

		protected static final String POLICY = "POLICY";

		protected static final String AUDIT = "AUDIT";

		protected static final String LOG = "LOG";

	}

}
