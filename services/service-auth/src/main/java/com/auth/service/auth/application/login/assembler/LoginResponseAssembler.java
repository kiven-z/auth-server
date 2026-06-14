package com.auth.service.auth.application.login.assembler;

import com.auth.service.auth.api.dto.authentication.response.UserLoginResponse;
import com.auth.service.auth.domain.authentication.model.AuthenticatedUser;
import com.auth.service.auth.domain.authentication.model.LoginResult;
import lombok.experimental.UtilityClass;

/**
 * 将领域登录结果组装为 API 响应。
 *
 * @author Bunny
 */
@UtilityClass
public class LoginResponseAssembler {

	/**
	 * 由 {@link LoginResult} 构建尚未填充令牌的登录响应（角色/权限来自授权画像）。
	 * @param loginResult 领域登录结果
	 * @return API 登录响应
	 */
	public static UserLoginResponse toUserLoginResponse(LoginResult loginResult) {
		AuthenticatedUser user = loginResult.authenticatedUser();

		// 创建用户登录响应
		UserLoginResponse response = new UserLoginResponse();
		response.setId(user.id());
		response.setUsername(user.username());
		response.setNickname(user.nickname());
		response.setAvatar(user.avatar());
		response.setRoles(loginResult.authProfile().getRoles());
		response.setPermissions(loginResult.authProfile().getPermissions());
		return response;
	}

}
