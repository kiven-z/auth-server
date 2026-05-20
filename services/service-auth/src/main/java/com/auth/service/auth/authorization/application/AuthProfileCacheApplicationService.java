package com.auth.service.auth.authorization.application;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;

import static com.auth.module.security.contract.redis.SecurityRedisKey.USER_PERM;

/**
 * 授权画像缓存：基于 Redis 的写入、读取与驱逐（原 {@code AuthProfileCache} 能力合并至此，避免空门面）。
 *
 * @author Bunny
 */
@Service
public class AuthProfileCacheApplicationService {

	private final RedisTemplate<String, Object> redisTemplate;

	private final ObjectMapper objectMapper;

	public AuthProfileCacheApplicationService(RedisTemplate<String, Object> redisTemplate,
			ObjectMapper objectMapper) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
	}

	/**
	 * 写入用户授权画像缓存。
	 * @param authProfile 授权画像
	 */
	public void cacheProfile(@NotNull AuthProfile authProfile) {
		Long userId = authProfile.getUserId();
		Assert.notNull(userId, "userId cannot be null");

		String userPerm = USER_PERM.key(userId);
		redisTemplate.opsForValue().set(userPerm, authProfile, USER_PERM.getDefaultTtl());
	}

	/**
	 * 读取用户授权画像缓存。
	 * @param userId 用户 ID
	 * @return 缓存中的画像，不存在时为空
	 */
	public Optional<AuthProfile> loadCachedProfile(long userId) {
		String userPermKey = USER_PERM.key(userId);
		Object value = redisTemplate.opsForValue().get(userPermKey);
		if (value == null) {
			return Optional.empty();
		}
		if (value instanceof AuthProfile profile) {
			return Optional.of(profile);
		}
		return Optional.ofNullable(objectMapper.convertValue(value, AuthProfile.class));
	}

	/**
	 * 移除用户授权画像缓存（登录失败回滚等场景）。
	 * @param userId 用户 ID
	 */
	public void evictProfile(long userId) {
		redisTemplate.delete(USER_PERM.key(userId));
	}

}
