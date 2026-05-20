package com.auth.service.auth.authorization.application;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.authorization.infrastructure.cache.AuthProfileCache;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 授权画像缓存应用服务：对外暴露缓存读写，隐藏 {@link AuthProfileCache} 基础设施细节。
 *
 * @author Bunny
 */
@Service
public class AuthProfileCacheApplicationService {

	private final AuthProfileCache authProfileCache;

	public AuthProfileCacheApplicationService(AuthProfileCache authProfileCache) {
		this.authProfileCache = authProfileCache;
	}

	/**
	 * 写入用户授权画像缓存。
	 * @param authProfile 授权画像
	 */
	public void cacheProfile(@NotNull AuthProfile authProfile) {
		authProfileCache.createCacheAuthProfile(authProfile);
	}

	/**
	 * 读取用户授权画像缓存。
	 * @param userId 用户 ID
	 * @return 缓存中的画像，不存在时为空
	 */
	public Optional<AuthProfile> loadCachedProfile(long userId) {
		return authProfileCache.loadCacheAuthProfile(userId);
	}

	/**
	 * 移除用户授权画像缓存（登录失败回滚等场景）。
	 * @param userId 用户 ID
	 */
	public void evictProfile(long userId) {
		authProfileCache.evictCacheAuthProfile(userId);
	}

}
