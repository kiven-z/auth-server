package com.auth.service.auth.domain.authentication.model;

import com.auth.module.platform.persistence.model.UserEntity;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 登录流程中的用户快照（领域层，与持久化实体解耦）。
 *
 * @author Bunny
 */
@Value
@Accessors(fluent = true)
public class LoginAccount {

	/**
	 * 用户 ID
	 */
	Long id;

	/**
	 * 用户名
	 */
	String username;

	/**
	 * 账户状态（与平台 {@code sys_user.status} 一致）
	 */
	Integer status;

	/**
	 * 加密后的密码（验证码登录场景可为 null）
	 */
	String encodedPassword;

	/**
	 * 昵称
	 */
	String nickname;

	/**
	 * 头像
	 */
	String avatar;

	/**
	 * 权限版本
	 */
	Long permVersion;

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
