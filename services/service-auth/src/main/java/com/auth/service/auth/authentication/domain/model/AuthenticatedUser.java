package com.auth.service.auth.authentication.domain.model;

import com.auth.module.platform.persistence.model.UserEntity;

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
	 * 从持久化用户实体构建。
	 * @param user 用户实体
	 * @return 认证用户快照
	 */
	public static AuthenticatedUser from(UserEntity user) {
		return new AuthenticatedUser(user.getId(), user.getUsername(), user.getNickname(), user.getAvatar());
	}

}
