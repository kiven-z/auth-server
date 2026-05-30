package com.auth.service.auth.authentication.domain.model;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import org.jetbrains.annotations.NotNull;

/**
 * 登录结果（领域层）：认证用户快照 + 授权画像，供应用层组装 API 响应与签发令牌。
 *
 * @param authenticatedUser 已认证用户展示信息
 * @param authProfile 授权画像
 * @param loginLogType 登录审计类型
 * @author Bunny
 */
public record LoginResult(@NotNull AuthenticatedUser authenticatedUser, @NotNull AuthProfile authProfile,
		@NotNull AuthLoginLogType loginLogType) {
}
