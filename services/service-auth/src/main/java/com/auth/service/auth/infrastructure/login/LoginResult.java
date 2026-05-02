package com.auth.service.auth.infrastructure.login;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.response.UserLoginResponse;
import org.jetbrains.annotations.NotNull;

/**
 * 登录结果（用于在登录链路中复用授权画像，避免重复查询）
 *
 * @param response 用户登录响应
 * @param authProfile 授权画像
 * @author Bunny
 */
public record LoginResult(@NotNull UserLoginResponse response, @NotNull AuthProfile authProfile,
		@NotNull AuthLoginLogType loginLogType) {
}
