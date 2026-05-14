package com.auth.module.security.contract.api.audit;

/**
 * 操作审计小模块：与具体业务能力/菜单域对齐，枚举名建议与 {@code SystemModule} 等业务码表一致。
 * <p>
 * 与 {@link AuditServiceDomain} 组合后经 {@link AuditOperationModuleKeys} 写入
 * {@code log_operation.module}。
 * </p>
 *
 * @author Bunny
 */
public enum AuditBizModule {

	// --- system 域（与 SystemModule 命名对齐，便于检索） ---

	/**
	 * 部门
	 */
	SYS_DEPT,
	/**
	 * 岗位
	 */
	SYS_POST,
	/**
	 * 用户
	 */
	SYS_USER,
	/**
	 * 角色
	 */
	SYS_ROLE,
	/**
	 * 菜单
	 */
	SYS_MENU,
	/**
	 * 菜单权限
	 */
	SYS_MENU_PERMISSION,
	/**
	 * 菜单角色
	 */
	SYS_MENU_ROLE,
	/**
	 * 权限
	 */
	SYS_PERMISSION,
	/**
	 * 角色权限
	 */
	SYS_ROLE_PERMISSION,
	/**
	 * 部门闭包
	 */
	DEPT_CLOSURE,
	/**
	 * 邮件模板
	 */
	SYS_EMAIL_TEMPLATE,
	/**
	 * 定时任务
	 */
	SYS_JOB,
	/**
	 * 定时任务分组
	 */
	SYS_JOB_GROUP,
	/**
	 * 文件
	 */
	SYS_FILES,
	/**
	 * 文件分片详情
	 */
	SYS_FILES_PART_DETAIL,
	/**
	 * 用户部门关联
	 */
	USER_DEPT,
	/**
	 * 用户岗位关联
	 */
	USER_POST,
	/**
	 * 授权主体
	 */
	GRANT_TABLE,
	/**
	 * 数据范围授权
	 */
	DATA_SCOPE_GRANT_TABLE,
	/**
	 * 访问策略绑定
	 */
	USER_ACCESS_POLICY_BINDING,
	/**
	 * 访问策略定义
	 */
	USER_ACCESS_POLICY_DEFINITION,
	/**
	 * 访问策略维度
	 */
	USER_ACCESS_POLICY_DIMENSION,
	/**
	 * 授权审计日志
	 */
	LOG_AUTHORIZATION_AUDIT,
	/**
	 * 数据范围审计日志
	 */
	LOG_DATA_SCOPE_AUDIT,
	/**
	 * 策略执行日志
	 */
	LOG_POLICY_EXECUTION,
	/**
	 * 密码历史日志
	 */
	LOG_USER_PASSWORD_HISTORY,
	/**
	 * 操作日志
	 */
	LOG_OPERATION,
	/**
	 * 登录日志
	 */
	LOG_LOGIN,
	/**
	 * 任务调度日志
	 */
	LOG_JOB,

	// --- auth 域 ---

	/**
	 * 认证与登录相关
	 */
	AUTH_LOGIN,

	// --- example 域 ---

	/**
	 * 集成演示接口
	 */
	EXAMPLE_DEMO

}
