package com.auth.service.system.common.model.audit;

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
     * 菜单权限
     */
    public static final String SYS_MENU_PERMISSION = "SYS_MENU_PERMISSION";

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
     * 文件
     */
    public static final String SYS_FILES = "SYS_FILES";

    /**
     * 文件分片详情
     */
    public static final String SYS_FILES_PART_DETAIL = "SYS_FILES_PART_DETAIL";

    /**
     * 用户部门关联
     */
    public static final String USER_DEPT = "USER_DEPT";

    /**
     * 用户岗位关联
     */
    public static final String USER_POST = "USER_POST";

    /**
     * 授权主体
     */
    public static final String GRANT_TABLE = "GRANT_TABLE";

    /**
     * 访问策略绑定
     */
    public static final String USER_ACCESS_POLICY_BINDING = "USER_ACCESS_POLICY_BINDING";

    /**
     * 访问策略定义
     */
    public static final String USER_ACCESS_POLICY_DEFINITION = "USER_ACCESS_POLICY_DEFINITION";

    /**
     * 访问策略维度
     */
    public static final String USER_ACCESS_POLICY_DIMENSION = "USER_ACCESS_POLICY_DIMENSION";

    /**
     * 授权审计日志
     */
    public static final String LOG_AUTHORIZATION_AUDIT = "LOG_AUTHORIZATION_AUDIT";

    /**
     * 数据范围审计日志
     */
    public static final String LOG_DATA_SCOPE_AUDIT = "LOG_DATA_SCOPE_AUDIT";

    /**
     * 策略执行日志
     */
    public static final String LOG_POLICY_EXECUTION = "LOG_POLICY_EXECUTION";

    /**
     * 密码历史日志
     */
    public static final String LOG_USER_PASSWORD_HISTORY = "LOG_USER_PASSWORD_HISTORY";

    /**
     * 操作日志
     */
    public static final String LOG_OPERATION = "LOG_OPERATION";

    /**
     * 登录日志
     */
    public static final String LOG_LOGIN = "LOG_LOGIN";

    /**
     * 任务调度日志
     */
    public static final String LOG_JOB = "LOG_JOB";

}
