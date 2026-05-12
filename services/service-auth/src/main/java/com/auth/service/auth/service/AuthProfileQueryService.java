package com.auth.service.auth.service;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.authorization.AuthProfile;

/**
 * 授权画像查询服务
 *
 * @author Bunny
 */
public interface AuthProfileQueryService {

	/**
	 * 根据用户ID构建授权画像
	 * @param userId 用户ID
	 * @return 授权画像
	 */
	AuthProfile buildByUserId(Long userId);

	/**
	 * 根据用户信息构建授权画像（避免重复查询用户）
	 * @param user 用户信息
	 * @return 授权画像
	 */
	AuthProfile buildByUser(UserEntity user);

}
