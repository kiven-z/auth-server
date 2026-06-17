package com.auth.service.auth.application.port.authorization;

import com.auth.module.security.contract.api.authorization.AuthProfile;

/**
 * 授权画像查询端口：供认证等上下文按用户 ID 加载 {@link AuthProfile}，由授权应用服务实现
 *
 * @author Bunny
 */
public interface AuthProfileQueryPort {

	/**
	 * 根据用户 ID 构建授权画像
	 * @param userId 用户 ID
	 * @return 授权画像
	 */
	AuthProfile buildByUserId(Long userId);

}
