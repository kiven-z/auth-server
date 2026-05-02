package com.auth.service.auth.model.response;

import com.auth.common.core.annotation.JsonStringFormat;
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

	/**
	 * 用户ID
	 */
	@JsonStringFormat
	private Long id;

	/**
	 * 用户名
	 */
	private String username;

	/**
	 * 昵称
	 */
	private String nickname;

	/**
	 * 头像
	 */
	private String avatar;

	/**
	 * 角色
	 */
	private List<String> roles;

	/**
	 * 权限
	 */
	private List<String> permissions;

}
