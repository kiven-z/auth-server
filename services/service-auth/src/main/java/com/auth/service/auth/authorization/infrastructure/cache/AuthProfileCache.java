package com.auth.service.auth.authorization.infrastructure.cache;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;

import static com.auth.module.security.contract.redis.SecurityRedisKey.USER_PERM;

/**
 * 关门处理 AuthProfile 会话相关
 *
 * @author Bunny
 */
@Service
public class AuthProfileCache {

	private final RedisTemplate<String, Object> redisTemplate;

	private final ObjectMapper objectMapper;

	public AuthProfileCache(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
	}

	/**
	 * 创建认证授权画像
	 * @param authProfile 认证配置文件
	 */
	public void createCacheAuthProfile(@NotNull AuthProfile authProfile) {
		Long userId = authProfile.getUserId();
		Assert.notNull(userId, "userId cannot be null");

		String userPerm = USER_PERM.key(userId);
		redisTemplate.opsForValue().set(userPerm, authProfile, USER_PERM.getDefaultTtl());
	}

	/**
	 * 读取用户授权画像（不存在返回空）
	 * @param userId 用户ID
	 * @return 授权画像
	 */
	public Optional<AuthProfile> loadCacheAuthProfile(long userId) {
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
	 * 移除用户授权画像缓存（登录编排失败补偿等场景）
	 * @param userId 用户 ID
	 */
	public void evictCacheAuthProfile(long userId) {
		redisTemplate.delete(USER_PERM.key(userId));
	}

}
