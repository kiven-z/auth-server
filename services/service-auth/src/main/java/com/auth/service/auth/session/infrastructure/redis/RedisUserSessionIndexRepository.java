package com.auth.service.auth.session.infrastructure.redis;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.authentication.application.port.UserSessionIndexPort;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.auth.module.security.contract.redis.SecurityRedisKey.USER_SESSION;
import static com.auth.module.security.contract.redis.SecurityRedisKey.USER_SESSIONS;

/**
 * 负责用户 UserSessionIndex 会话创建和查询
 *
 * @author Bunny
 */
@Service
public class RedisUserSessionIndexRepository implements UserSessionIndexPort {

	private final RedisTemplate<String, Object> redisTemplate;

	public RedisUserSessionIndexRepository(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * 统计用户活跃会话数
	 * @param userId 用户ID
	 * @return 活跃会话数
	 */
	public long countActiveSessions(long userId) {
		String key = USER_SESSIONS.key(userId);
		Long count = redisTemplate.opsForSet().size(key);
		return count != null ? count : 0L;
	}

	/**
	 * 添加会话，使用 jti 向Redis中添加会话
	 * @param userSessionIndex 用户会话索引
	 */
	@Override
	public void createUserSessionIndex(@NotNull UserSessionIndex userSessionIndex) {
		// jti
		String sessionId = userSessionIndex.getSessionId();
		String userSessionKey = USER_SESSION.key(sessionId);

		// userId
		Long userId = userSessionIndex.getUserId();
		String userSessionsKey = USER_SESSIONS.key(userId);

		// 将 UserSessionIndex 转为 Map 作为 Hash 字段（不含手写 map.put，一行完成）
		Map<String, Object> sessionMap = BeanUtil.beanToMap(userSessionIndex);

		// 写入会话详情 Hash
		redisTemplate.opsForHash().putAll(userSessionKey, sessionMap);

		// 将当前 jti 加入用户活跃会话集合
		redisTemplate.opsForSet().add(userSessionsKey, sessionId);

		// 根据 refreshToken 过期时间戳为 Hash 设置 TTL，保证会话自然过期
		Long refreshTokenExpiresAt = userSessionIndex.getRefreshTokenExpiresAt();
		Duration ttl = USER_SESSION.getDefaultTtl();
		if (refreshTokenExpiresAt != null) {
			long ttlSeconds = (refreshTokenExpiresAt - System.currentTimeMillis()) / 1000;
			if (ttlSeconds > 0) {
				ttl = Duration.ofSeconds(ttlSeconds);
			}
		}
		if (ttl != null) {
			redisTemplate.expire(userSessionKey, ttl);
		}
	}

	/**
	 * 读取会话索引（UserSessionIndex）（不存在返回空）
	 * @param jti 会话唯一标识
	 * @return 会话索引
	 */
	public Optional<UserSessionIndex> loadUserSessionIndex(@NotNull String jti) {
		if (CharSequenceUtil.isBlank(jti)) {
			return Optional.empty();
		}
		String sessionKey = USER_SESSION.key(jti);
		Map<Object, Object> hash = redisTemplate.opsForHash().entries(sessionKey);
		if (CollUtil.isEmpty(hash)) {
			return Optional.empty();
		}
		UserSessionIndex sessionIndex = BeanUtil.toBean(hash, UserSessionIndex.class);
		sessionIndex.setSessionId(jti);
		return Optional.of(sessionIndex);
	}

	/**
	 * 更新刷新令牌校验信息（Hash字段）并刷新会话 TTL
	 * @param jti 会话唯一标识
	 * @param refreshTokenHash refreshToken 哈希
	 * @param refreshTokenExpiresAt refreshToken 过期时间戳（毫秒）
	 * @param ttl 会话 TTL（通常与 refreshToken 有效期一致）
	 */
	public void updateRefreshInfo(@NotNull String jti, @NotNull String refreshTokenHash, long refreshTokenExpiresAt,
			@NotNull Duration ttl) {
		String sessionKey = USER_SESSION.key(jti);
		redisTemplate.opsForHash().put(sessionKey, "refreshTokenHash", refreshTokenHash);
		redisTemplate.opsForHash().put(sessionKey, "refreshTokenExpiresAt", refreshTokenExpiresAt);
		redisTemplate.expire(sessionKey, ttl);
	}

	/**
	 * 获取用户所有会话
	 * @param userId 用户ID
	 * @return 会话列表
	 */
	public List<UserSessionIndex> listUserSessions(long userId) {
		String userSessionsKey = USER_SESSIONS.key(userId);
		Set<Object> members = Optional.ofNullable(redisTemplate.opsForSet().members(userSessionsKey))
			.orElseGet(Collections::emptySet);

		return members.stream()
			.map(member -> member instanceof String jti ? jti : "")
			.filter(CharSequenceUtil::isNotBlank)
			.map(jti -> {
				String sessionKey = USER_SESSION.key(jti);
				Map<Object, Object> hash = redisTemplate.opsForHash().entries(sessionKey);
				// 直接用 BeanUtil 将 Map 转为 UserSessionIndex
				UserSessionIndex sessionIndex = BeanUtil.toBean(hash, UserSessionIndex.class);
				sessionIndex.setUserId(userId);
				sessionIndex.setSessionId(jti);
				return sessionIndex;
			})
			.toList();
	}

	/**
	 * 删除会话详情 key（不存在也视为成功）
	 * @param jti 会话唯一标识
	 */
	@Override
	public void deleteSession(@NotNull String jti) {
		String userSessionKey = USER_SESSION.key(jti);
		redisTemplate.delete(userSessionKey);
	}

	/**
	 * 从用户活跃会话索引移除指定 jti
	 * @param userId 用户ID
	 * @param jti 会话唯一标识
	 */
	@Override
	public void removeActiveSession(long userId, @NotNull String jti) {
		String userSessionsKey = USER_SESSIONS.key(userId);
		redisTemplate.opsForSet().remove(userSessionsKey, jti);
	}

	/**
	 * 删除用户所有会话
	 * @param userId 用户ID
	 */
	public void deleteAllSessions(long userId) {
		String userSessionsKey = USER_SESSIONS.key(userId);
		Set<Object> members = redisTemplate.opsForSet().members(userSessionsKey);
		redisTemplate.delete(userSessionsKey);

		if (CollUtil.isEmpty(members)) {
			return;
		}

		for (Object member : members) {
			if (member instanceof String jti) {
				deleteSession(jti);
			}
		}
	}

	/**
	 * 清理活跃会话集合中的僵尸会话（session 详情已不存在）
	 * @param userId 用户ID
	 * @return 清理数量
	 */
	public int cleanupStaleActiveSessions(long userId) {
		String activeSessionKey = USER_SESSIONS.key(userId);
		Set<Object> members = Optional.ofNullable(redisTemplate.opsForSet().members(activeSessionKey))
			.orElseGet(Collections::emptySet);

		return (int) members.stream()
			.filter(String.class::isInstance)
			.map(String.class::cast)
			.filter(jti -> !redisTemplate.hasKey(USER_SESSION.key(jti)))
			.filter(jti -> {
				Long num = redisTemplate.opsForSet().remove(activeSessionKey, jti);
				return num != null && num > 0;
			})
			.count();
	}

	/**
	 * 超限时淘汰最旧会话并返回被淘汰的会话ID
	 * @param userId 用户ID
	 * @return 被淘汰会话ID，若无可淘汰会话则为空
	 */
	public Optional<String> evictOldestActiveSession(long userId) {
		String activeSessionKey = USER_SESSIONS.key(userId);
		return Optional.ofNullable(redisTemplate.opsForSet().members(activeSessionKey))
			.filter(members -> !members.isEmpty())
			.stream()
			.flatMap(Collection::stream)
			.filter(String.class::isInstance)
			.map(String.class::cast)
			.min(Comparator.comparingLong(this::ttlRank))
			.map(evictedJti -> {
				removeActiveSession(userId, evictedJti);
				deleteSession(evictedJti);
				return evictedJti;
			});
	}

	/**
	 * 获取会话剩余TTL
	 * @param jti 会话唯一标识
	 * @return 剩余TTL
	 */
	private long ttlRank(String jti) {
		String userSessionKey = USER_SESSION.key(jti);
		long ttl = redisTemplate.getExpire(userSessionKey, TimeUnit.SECONDS);

		// -2: key 不存在；-1: 无过期都视为较新，避免误淘汰有效长会话
		if (ttl < 0) {
			return Long.MAX_VALUE;
		}
		return ttl;
	}

}
