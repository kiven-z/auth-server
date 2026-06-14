package com.auth.service.auth.domain.authentication.model;

/**
 * 登出审计上下文：撤销会话后供编排层写入单条登出日志。
 *
 * @param userId 用户 ID
 * @param jti 会话 ID
 * @author Bunny
 */
public record LogoutSessionHint(Long userId, String jti) {

}
