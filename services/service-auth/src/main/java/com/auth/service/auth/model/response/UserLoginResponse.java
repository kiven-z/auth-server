package com.auth.service.auth.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户登录响应（在刷新响应基础上补充展示字段）
 *
 * @author Bunny
 */
@Getter
@Setter
public class UserLoginResponse extends RefreshTokenResponse {

	@Schema(title = "昵称")
	private String nickname;

	@Schema(title = "头像")
	private String avatar;

}
