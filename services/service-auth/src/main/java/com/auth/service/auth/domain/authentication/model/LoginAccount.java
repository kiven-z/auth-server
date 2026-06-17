package com.auth.service.auth.domain.authentication.model;

import com.auth.module.platform.persistence.model.UserEntity;

/**
 * 登录流程中的用户快照（领域层，与持久化实体解耦）。
 *
 * @param id 用户 ID
 * @param username 用户名
 * @param status 账户状态（与平台 {@code sys_user.status} 一致）
 * @param encodedPassword 加密后的密码（验证码登录场景可为 null）
 * @param nickname 昵称
 * @param avatar 头像
 * @param permVersion 权限版本
 * @author Bunny
 */
public record LoginAccount(Long id, String username, Integer status, String encodedPassword, String nickname,
		String avatar, Long permVersion) {

	/**
	 * 从持久化用户实体构建登录账户快照。
	 * @param user 用户实体
	 * @return 登录账户
	 */
	public static LoginAccount from(UserEntity user) {
		return new LoginAccount(user.getId(), user.getUsername(), user.getStatus(), user.getPassword(),
				user.getNickname(), user.getAvatar(), user.getPermVersion());
	}

}
