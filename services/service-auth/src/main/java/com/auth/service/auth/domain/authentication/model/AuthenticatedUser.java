package com.auth.service.auth.domain.authentication.model;

import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 登录成功后的用户展示信息（领域层，与 API Response 解耦）。
 *
 * @author Bunny
 */
@Value
@Accessors(fluent = true)
public class AuthenticatedUser {

	/**
	 * 用户 ID
	 */
	Long id;

	/**
	 * 用户名
	 */
	String username;

	/**
	 * 昵称
	 */
	String nickname;

	/**
	 * 头像
	 */
	String avatar;

	/**
	 * 从登录账户快照构建。
	 * @param account 登录账户
	 * @return 认证用户快照
	 */
	public static AuthenticatedUser from(LoginAccount account) {
		return new AuthenticatedUser(account.id(), account.username(), account.nickname(), account.avatar());
	}

}
