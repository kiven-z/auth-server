package com.auth.service.auth.application.authorization.datascope;

/**
 * user_scope 只读行
 *
 * @author Bunny
 * @param scopeType 范围类型
 * @param scopeDeptIds scope_dept_ids JSON 字符串
 */
public record UserScopeRow(String scopeType, String scopeDeptIds) {

}
