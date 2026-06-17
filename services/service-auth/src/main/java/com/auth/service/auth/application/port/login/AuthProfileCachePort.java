package com.auth.service.auth.application.port.login;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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

	/**
	 * 读取用户授权画像缓存。
	 * @param userId 用户 ID
	 * @return 缓存中的画像，不存在时为空
	 */
	Optional<AuthProfile> loadCachedProfile(long userId);

}
