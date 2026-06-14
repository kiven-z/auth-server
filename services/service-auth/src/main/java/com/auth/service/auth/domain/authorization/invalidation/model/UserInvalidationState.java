package com.auth.service.auth.domain.authorization.invalidation.model;

/**
 * 用户失效策略输入：仅含权限同步所需的状态位
 *
 * @param userId 用户 ID
 * @param status 用户状态（0=禁用,1=正常,2=锁定）
 * @param deleted 是否逻辑删除
 * @author Bunny
 */
public record UserInvalidationState(Long userId, Integer status, boolean deleted) {

}
