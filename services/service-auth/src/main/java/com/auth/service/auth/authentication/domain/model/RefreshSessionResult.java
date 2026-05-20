package com.auth.service.auth.authentication.domain.model;

import com.auth.service.auth.authentication.api.dto.response.RefreshTokenResponse;

/**
 * 刷新令牌业务结果：响应体 + 会话级「记住我」标记（用于重写 Refresh Cookie）。
 *
 * @param response 刷新令牌响应（含 readMeDay）
 * @param rememberMe 登录时是否勾选记住我
 * @author Bunny
 */
public record RefreshSessionResult(RefreshTokenResponse response, boolean rememberMe) {
}
