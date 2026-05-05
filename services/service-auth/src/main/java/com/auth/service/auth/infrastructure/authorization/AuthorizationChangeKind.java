package com.auth.service.auth.infrastructure.authorization;

/**
 * 授权主体类别
 *
 * @author Bunny
 */
public enum AuthorizationChangeKind {
    /**
     * 角色
     */
    ROLE,

    /**
     * 权限
     */
    PERMISSION,

    /**
     * 授权
     */
    GRANT,

    /**
     * 用户部门
     */
    USER_DEPT
}
