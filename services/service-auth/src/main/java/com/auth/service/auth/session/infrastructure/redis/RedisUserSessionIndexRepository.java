package com.auth.service.auth.session.infrastructure.redis;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.api.UserSessionIndex;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.auth.module.security.contract.redis.SecurityRedisKey.*;

/**
 * 负责用户 UserSessionIndex 会话创建和查询
 *
 * @author Bunny
 */
@Service
public class RedisUserSessionIndexRepository {

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
	 * 注册活跃会话：写入 Hash、用户 Set 与全局 ZSet（原子 MULTI/EXEC）
	 * @param userSessionIndex 用户会话索引
	 */
	public void registerSession(@NotNull UserSessionIndex userSessionIndex) {
		String sessionId = userSessionIndex.getSessionId();
		Long userId = userSessionIndex.getUserId();
		if (CharSequenceUtil.isBlank(sessionId) || userId == null) {
			throw new IllegalArgumentException("sessionId and userId are required for session registration");
		}

		String userSessionKey = USER_SESSION.key(sessionId);
		String userSessionsKey = USER_SESSIONS.key(userId);
		String globalSessionsKey = GLOBAL_SESSIONS.fixedKey();
		Map<String, Object> sessionMap = BeanUtil.beanToMap(userSessionIndex);
		Duration ttl = resolveSessionTtl(userSessionIndex);
		double loginAtScore = resolveLoginAtScore(userSessionIndex);

		redisTemplate.execute(new SessionCallback<>() {
			@Override
			@SuppressWarnings("unchecked")
			public List<Object> execute(@NotNull RedisOperations operations) throws DataAccessException {
				operations.multi();
				operations.opsForHash().putAll(userSessionKey, sessionMap);
				if (ttl != null) {
					operations.expire(userSessionKey, ttl);
				}
				operations.opsForSet().add(userSessionsKey, sessionId);
				operations.opsForZSet().add(globalSessionsKey, sessionId, loginAtScore);
				return operations.exec();
			}
		});
	}

	/**
	 * 终止单条会话：删除 Hash、用户 Set 与全局 ZSet（原子 MULTI/EXEC，幂等）
	 * @param userId 用户 ID
	 * @param jti 会话唯一标识
	 */
	public void terminateSession(long userId, @NotNull String jti) {
		if (CharSequenceUtil.isBlank(jti)) {
			return;
		}
		String userSessionKey = USER_SESSION.key(jti);
		String userSessionsKey = USER_SESSIONS.key(userId);
		String globalSessionsKey = GLOBAL_SESSIONS.fixedKey();

		redisTemplate.execute(new SessionCallback<>() {
			@Override
			@SuppressWarnings("unchecked")
			public List<Object> execute(@NotNull RedisOperations operations) throws DataAccessException {
				operations.multi();
				operations.delete(userSessionKey);
				operations.opsForSet().remove(userSessionsKey, jti);
				operations.opsForZSet().remove(globalSessionsKey, jti);
				return operations.exec();
			}
		});
	}

	/**
	 * 终止单条会话：从 Hash 解析 userId；Hash 已丢失时仍清理全局 ZSet
	 * @param jti 会话唯一标识
	 */
	public void terminateSession(@NotNull String jti) {
		if (CharSequenceUtil.isBlank(jti)) {
			return;
		}
		Optional<UserSessionIndex> sessionIndex = loadUserSessionIndex(jti);
		if (sessionIndex.isPresent() && sessionIndex.get().getUserId() != null) {
			terminateSession(sessionIndex.get().getUserId(), jti);
			return;
		}
		terminateSessionWithoutUserIndex(jti);
	}

	/**
	 * 终止用户全部活跃会话
	 * @param userId 用户 ID
	 */
	public void terminateAllSessions(long userId) {
		String userSessionsKey = USER_SESSIONS.key(userId);
		Set<Object> members = Optional.ofNullable(redisTemplate.opsForSet().members(userSessionsKey))
			.orElseGet(Collections::emptySet);
		redisTemplate.delete(userSessionsKey);

		if (CollUtil.isEmpty(members)) {
			return;
		}

		for (Object member : members) {
			if (member instanceof String jti) {
				terminateSession(userId, jti);
			}
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
				UserSessionIndex sessionIndex = BeanUtil.toBean(hash, UserSessionIndex.class);
				sessionIndex.setUserId(userId);
				sessionIndex.setSessionId(jti);
				return sessionIndex;
			})
			.toList();
	}

	/**
	 * 清理活跃会话集合中的僵尸会话（session 详情已不存在）
	 * @param userId 用户ID
	 * @return 清理数量
	 */
	public int cleanupStaleActiveSessions(long userId) {
		String activeSessionKey = USER_SESSIONS.key(userId);
		String globalSessionsKey = GLOBAL_SESSIONS.fixedKey();
		Set<Object> members = Optional.ofNullable(redisTemplate.opsForSet().members(activeSessionKey))
			.orElseGet(Collections::emptySet);

		return (int) members.stream()
			.filter(String.class::isInstance)
			.map(String.class::cast)
			.filter(jti -> !redisTemplate.hasKey(USER_SESSION.key(jti)))
			.filter(jti -> {
				Long setRemoved = redisTemplate.opsForSet().remove(activeSessionKey, jti);
				Long zsetRemoved = redisTemplate.opsForZSet().remove(globalSessionsKey, jti);
				return (setRemoved != null && setRemoved > 0) || (zsetRemoved != null && zsetRemoved > 0);
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
				terminateSession(userId, evictedJti);
				return evictedJti;
			});
	}

	/**
	 * Hash 已丢失时仍清理全局 ZSet 残留
	 * @param jti 会话唯一标识
	 */
	private void terminateSessionWithoutUserIndex(@NotNull String jti) {
		String userSessionKey = USER_SESSION.key(jti);
		String globalSessionsKey = GLOBAL_SESSIONS.fixedKey();
		redisTemplate.execute(new SessionCallback<>() {
			@Override
			@SuppressWarnings("unchecked")
			public List<Object> execute(@NotNull RedisOperations operations) throws DataAccessException {
				operations.multi();
				operations.delete(userSessionKey);
				operations.opsForZSet().remove(globalSessionsKey, jti);
				return operations.exec();
			}
		});
	}

	/**
	 * 根据 refreshToken 过期时间计算会话 TTL
	 * @param userSessionIndex 会话索引
	 * @return TTL，无法计算时返回默认 TTL
	 */
	private Duration resolveSessionTtl(@NotNull UserSessionIndex userSessionIndex) {
		Long refreshTokenExpiresAt = userSessionIndex.getRefreshTokenExpiresAt();
		Duration ttl = USER_SESSION.getDefaultTtl();
		if (refreshTokenExpiresAt != null) {
			long ttlSeconds = (refreshTokenExpiresAt - System.currentTimeMillis()) / 1000;
			if (ttlSeconds > 0) {
				ttl = Duration.ofSeconds(ttlSeconds);
			}
		}
		return ttl;
	}

	/**
	 * 解析全局 ZSet 排序分值
	 * @param userSessionIndex 会话索引
	 * @return loginAt 毫秒时间戳
	 */
	private double resolveLoginAtScore(@NotNull UserSessionIndex userSessionIndex) {
		Long loginAt = userSessionIndex.getLoginAt();
		if (loginAt != null) {
			return loginAt.doubleValue();
		}
		return System.currentTimeMillis();
	}

	/**
	 * 获取会话剩余TTL
	 * @param jti 会话唯一标识
	 * @return 剩余TTL
	 */
	private Long ttlRank(String jti) {
		String userSessionKey = USER_SESSION.key(jti);
		Long ttl = redisTemplate.getExpire(userSessionKey, TimeUnit.SECONDS);

		// null: Redis 未返回 TTL；-2: key 不存在；-1: 无过期都视为较新，避免误淘汰有效长会话
		if (ttl < 0) {
			return Long.MAX_VALUE;
		}
		return ttl;
	}

}
