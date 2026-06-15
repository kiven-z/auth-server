package com.auth.service.auth.application.authorization.datascope;

/**
 * role_scope 只读行
 *
 * @author Bunny
 * @param roleCode 角色编码
 * @param scopeType 范围类型
 * @param scopeDeptIds scope_dept_ids JSON 字符串
 */
public record RoleScopeRow(String roleCode, String scopeType, String scopeDeptIds) {

}
