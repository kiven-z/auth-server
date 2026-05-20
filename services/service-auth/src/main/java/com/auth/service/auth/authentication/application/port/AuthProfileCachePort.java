package com.auth.service.auth.authentication.application.port;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import org.jetbrains.annotations.NotNull;

/**
 * 授权画像 Redis 缓存端口。
 *
 * @author Bunny
 */
public interface AuthProfileCachePort {

	/**
	 * 写入用户授权画像缓存。
	 * @param authProfile 授权画像
	 */
	void cacheProfile(@NotNull AuthProfile authProfile);

	/**
	 * 驱逐指定用户的授权画像缓存。
	 * @param userId 用户 ID
	 */
	void evictProfile(long userId);

}
