package com.auth.service.system.common.model;

import lombok.Getter;

/**
 * 系统模块枚举，映射数据库中的业务表。
 * <p>
 * 用于前端远程搜索下拉、权限配置等场景，value 对应实际的数据库表名（含前缀），
 * type 为模块分类，便于前端分组显示。
 * </p>
 *
 * @author Bunny
 */
@Getter
public enum SystemModule {

    // ========== 系统业务（SYS） ==========

    /**
     * 部门表
     */
    SYS_DEPT("SYS", "部门"),
    /**
     * 岗位表
     */
    SYS_POST("SYS", "岗位"),
    /**
     * 用户主表
     */
    SYS_USER("SYS", "用户"),
    /**
     * 角色表
     */
    SYS_ROLE("SYS", "角色"),
    /**
     * 菜单表
     */
    SYS_MENU("SYS", "菜单"),
    /**
     * 菜单-权限关联表
     */
    SYS_MENU_PERMISSION("SYS", "菜单权限"),
    /**
     * 菜单-角色关联表
     */
    SYS_MENU_ROLE("SYS", "菜单角色"),
    /**
     * 权限表（按钮/接口级）
     */
    SYS_PERMISSION("SYS", "权限"),
    /**
     * 角色-权限关联表
     */
    SYS_ROLE_PERMISSION("SYS", "角色权限"),
    /**
     * 部门闭包表（树形结构）
     */
    DEPT_CLOSURE("SYS", "部门闭包"),
    /**
     * 邮件模板配置表
     */
    SYS_EMAIL_TEMPLATE("SYS", "邮件模板"),
    /**
     * 定时任务表
     */
    SYS_JOB("SYS", "定时任务"),
    /**
     * 定时任务分组表
     */
    SYS_JOB_GROUP("SYS", "定时任务分组"),
    /**
     * 文件表
     */
    SYS_FILES("SYS", "文件"),
    /**
     * 文件分片详情表
     */
    SYS_FILES_PART_DETAIL("SYS", "文件分片详情"),

    // ========== 用户相关（USER） ==========

    /**
     * 用户-部门关联表
     */
    USER_DEPT("USER", "用户部门关联"),
    /**
     * 用户-岗位关联表
     */
    USER_POST("USER", "用户岗位关联"),
    /**
     * 用户密码历史表
     */
    USER_PASSWORD_HISTORY("USER", "密码历史"),

    // ========== 权限/授权（GRANT） ==========

    /**
     * 授权主体表（Grant Table）
     */
    GRANT_TABLE("GRANT", "授权主体"),
    /**
     * 数据范围授权表
     */
    DATA_SCOPE_GRANT_TABLE("GRANT", "数据范围授权"),

    // ========== 访问策略（POLICY） ==========

    /**
     * 用户访问策略绑定表
     */
    USER_ACCESS_POLICY_BINDING("POLICY", "访问策略绑定"),
    /**
     * 用户访问策略定义表
     */
    USER_ACCESS_POLICY_DEFINITION("POLICY", "访问策略定义"),
    /**
     * 用户访问策略维度表
     */
    USER_ACCESS_POLICY_DIMENSION("POLICY", "访问策略维度"),

    // ========== 审计日志（AUDIT） ==========

    /**
     * 授权审计日志
     */
    LOG_AUTHORIZATION_AUDIT("AUDIT", "授权审计日志"),
    /**
     * 数据范围审计日志
     */
    LOG_DATA_SCOPE_AUDIT("AUDIT", "数据范围审计日志"),
    /**
     * 策略执行日志
     */
    LOG_POLICY_EXECUTION("AUDIT", "策略执行日志"),

    // ========== 操作日志（LOG） ==========

    /**
     * 操作日志
     */
    LOG_OPERATION("LOG", "操作日志"),
    /**
     * 登录日志
     */
    LOG_LOGIN("LOG", "登录日志"),
    /**
     * 任务调度日志
     */
    LOG_JOB("LOG", "任务调度日志"),
    ;

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

}
