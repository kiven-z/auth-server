package com.auth.service.auth.authentication.api.dto.response;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 用户登录响应
 *
 * @author Bunny
 */
@Getter
@Setter
public class UserLoginResponse extends RefreshTokenResponse {

	@JsonStringFormat
	@Schema(description = "用户ID")
	private Long id;

	@Schema(description = "用户名")
	private String username;

	@Schema(description = "昵称")
	private String nickname;

	@Schema(description = "头像")
	private String avatar;

	@Schema(description = "角色")
	private List<String> roles;

	@Schema(description = "权限")
	private List<String> permissions;

}
