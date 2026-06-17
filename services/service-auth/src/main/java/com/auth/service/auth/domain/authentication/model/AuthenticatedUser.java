package com.auth.service.auth.domain.authentication.model;

/**
 * 登录成功后的用户展示信息（领域层，与 API Response 解耦）。
 *
 * @param id 用户 ID
 * @param username 用户名
 * @param nickname 昵称
 * @param avatar 头像
 * @author Bunny
 */
public record AuthenticatedUser(Long id, String username, String nickname, String avatar) {

	/**
	 * 从登录账户快照构建。
	 * @param account 登录账户
	 * @return 认证用户快照
	 */
	public static AuthenticatedUser from(LoginAccount account) {
		return new AuthenticatedUser(account.id(), account.username(), account.nickname(), account.avatar());
	}

}
