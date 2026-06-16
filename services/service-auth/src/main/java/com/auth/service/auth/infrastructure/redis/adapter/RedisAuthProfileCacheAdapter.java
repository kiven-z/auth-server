package com.auth.service.auth.infrastructure.redis.adapter;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.application.port.login.AuthProfileCachePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.Optional;

import static com.auth.module.security.contract.redis.SecurityRedisKey.USER_PERM;

/**
 * 授权画像 Redis 缓存 Port 适配器
 *
 * @author Bunny
 */
@Repository
public class RedisAuthProfileCacheAdapter implements AuthProfileCachePort {

	private final RedisTemplate<String, Object> redisTemplate;

	private final ObjectMapper objectMapper;

	public RedisAuthProfileCacheAdapter(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cacheProfile(@NotNull AuthProfile authProfile) {
		Long userId = authProfile.getUserId();
		Assert.notNull(userId, "用户ID不能为空");

		redisTemplate.opsForValue().set(USER_PERM.key(userId), authProfile, USER_PERM.getDefaultTtl());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<AuthProfile> loadCachedProfile(long userId) {
		Object value = redisTemplate.opsForValue().get(USER_PERM.key(userId));

		if (value == null) {
			return Optional.empty();
		}

		if (value instanceof AuthProfile profile) {
			return Optional.of(profile);
		}

		AuthProfile profile = objectMapper.convertValue(value, AuthProfile.class);
		return Optional.ofNullable(profile);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void evictProfile(long userId) {
		String key = USER_PERM.key(userId);
		redisTemplate.delete(key);
	}

}
