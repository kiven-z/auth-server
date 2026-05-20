package com.auth.service.auth.authentication.domain.model;

/**
 * 刷新令牌业务结果：新令牌对 + 会话级「记住我」标记（用于重写 Refresh Cookie）。
 *
 * @param tokenPair 访问/刷新令牌对
 * @param rememberMe 登录时是否勾选记住我
 * @author Bunny
 */
public record RefreshSessionResult(TokenPair tokenPair, boolean rememberMe) {
}
