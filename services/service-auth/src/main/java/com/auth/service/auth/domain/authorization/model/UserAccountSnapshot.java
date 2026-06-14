package com.auth.service.auth.domain.authorization.model;

import com.auth.module.platform.persistence.model.UserEntity;

/**
 * 授权上下文用户只读快照（与持久化实体、登录域 {@code LoginAccount} 解耦）
 *
 * @param id 用户 ID
 * @param username 用户名
 * @param permVersion 权限版本
 * @author Bunny
 */
public record UserAccountSnapshot(Long id, String username, Long permVersion) {

	/**
	 * 从持久化用户实体构建授权快照
	 * @param user 用户实体
	 * @return 授权快照
	 */
	public static UserAccountSnapshot from(UserEntity user) {
		return new UserAccountSnapshot(user.getId(), user.getUsername(), user.getPermVersion());
	}

}
