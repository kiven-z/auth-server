package com.auth.service.auth.application.port.authorization.model;

/**
 * user_scope 只读行
 *
 * @param scopeType 范围类型
 * @param scopeDeptIds scope_dept_ids JSON 字符串
 * @author Bunny
 */
public record UserScopeRow(String scopeType, String scopeDeptIds) {

}
