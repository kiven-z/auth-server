package com.auth.service.auth.session.infrastructure.redis;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.session.infrastructure.redis.script.UserSessionRedisScripts;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.auth.module.security.contract.redis.SecurityRedisKey.*;

/**
 * 用户会话 Redis 索引：注册、终止、查询与僵尸索引清理
 *
 * @author Bunny
 */
@Service
public class RedisUserSessionIndexRepository {

	private final RedisTemplate<String, Object> redisTemplate;

	private final UserSessionRedisScripts userSessionRedisScripts;

	public RedisUserSessionIndexRepository(RedisTemplate<String, Object> redisTemplate,
			UserSessionRedisScripts userSessionRedisScripts) {
		this.redisTemplate = redisTemplate;
		this.userSessionRedisScripts = userSessionRedisScripts;
	}

	/**
	 * 统计用户活跃会话数
	 * @param userId 用户ID
	 * @return 活跃会话数
	 */
	public long countActiveSessions(long userId) {
		return ObjectUtil.defaultIfNull(redisTemplate.opsForSet().size(USER_SESSIONS.key(userId)), 0L);
	}

	/**
	 * 注册活跃会话：写入 Hash、用户 Set 与全局 ZSet（Lua 原子）
	 * @param userSessionIndex 用户会话索引
	 */
	public void registerSession(@NotNull UserSessionIndex userSessionIndex) {
		String sessionId = userSessionIndex.getSessionId();
		Long userId = userSessionIndex.getUserId();
		if (CharSequenceUtil.isBlank(sessionId) || userId == null) {
			throw new IllegalArgumentException("sessionId and userId are required for session registration");
		}

		List<String> keys = List.of(USER_SESSION.key(sessionId), USER_SESSIONS.key(userId), GLOBAL_SESSIONS.fixedKey());
		Object[] args = buildRegisterScriptArgs(userSessionIndex, sessionId, resolveLoginAtScore(userSessionIndex),
				resolveSessionTtl(userSessionIndex));
		redisTemplate.execute(userSessionRedisScripts.registerScript(), keys, args);
	}

	/**
	 * 终止单条会话：删除 Hash、用户 Set 与全局 ZSet（Lua 原子，幂等）
	 * @param userId 用户 ID
	 * @param jti 会话唯一标识
	 */
	public void terminateSession(long userId, @NotNull String jti) {
		if (CharSequenceUtil.isBlank(jti)) {
			return;
		}
		List<String> keys = List.of(USER_SESSION.key(jti), USER_SESSIONS.key(userId), GLOBAL_SESSIONS.fixedKey());
		redisTemplate.execute(userSessionRedisScripts.terminateScript(), keys, jti);
	}

	/**
	 * 终止用户全部活跃会话
	 * @param userId 用户 ID
	 */
	public void terminateAllSessions(long userId) {
		String userSessionsKey = USER_SESSIONS.key(userId);
		List<String> jtis = streamActiveJtis(userSessionsKey).toList();
		redisTemplate.delete(userSessionsKey);
		jtis.forEach(jti -> terminateSession(userId, jti));
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
		Map<Object, Object> hash = redisTemplate.opsForHash().entries(USER_SESSION.key(jti));
		if (CollUtil.isEmpty(hash)) {
			return Optional.empty();
		}
		return Optional.of(toSessionIndex(jti, hash, null));
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
	 * 获取用户所有活跃会话（Hash 不存在时清理 Set/ZSet 残留）
	 * @param userId 用户ID
	 * @return 会话列表
	 */
	public List<UserSessionIndex> listUserSessions(long userId) {
		String userSessionsKey = USER_SESSIONS.key(userId);
		return streamActiveJtis(userSessionsKey).map(jti -> loadListedSession(userSessionsKey, userId, jti))
			.flatMap(Optional::stream)
			.toList();
	}

	/**
	 * 清理活跃会话集合中的僵尸会话（session 详情已不存在）
	 * @param userId 用户ID
	 * @return 清理数量
	 */
	public int cleanupStaleActiveSessions(long userId) {
		String userSessionsKey = USER_SESSIONS.key(userId);
		return (int) streamActiveJtis(userSessionsKey)
			.filter(jti -> CollUtil.isEmpty(redisTemplate.opsForHash().entries(USER_SESSION.key(jti))))
			.filter(jti -> removeStaleIndexes(userSessionsKey, jti))
			.count();
	}

	/**
	 * 超限时淘汰最旧会话并返回被淘汰的会话ID
	 * @param userId 用户ID
	 * @return 被淘汰会话ID，若无可淘汰会话则为空
	 */
	public Optional<String> evictOldestActiveSession(long userId) {
		String userSessionsKey = USER_SESSIONS.key(userId);
		return streamActiveJtis(userSessionsKey)
			.map(jti -> loadListedSession(userSessionsKey, userId, jti).map(index -> jti))
			.flatMap(Optional::stream)
			.min(Comparator.comparingLong(this::ttlRank))
			.map(evictedJti -> {
				terminateSession(userId, evictedJti);
				return evictedJti;
			});
	}

	/**
	 * 从 Hash 解析 userId 后终止；Hash 已丢失时仍清理全局 ZSet
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
		List<String> keys = List.of(USER_SESSION.key(jti), GLOBAL_SESSIONS.fixedKey());
		redisTemplate.execute(userSessionRedisScripts.terminateOrphanScript(), keys, jti);
	}

	/**
	 * 用户活跃会话 Set 中的 jti 流
	 * @param userSessionsKey 用户会话 Set key
	 * @return 非空 jti 流
	 */
	private Stream<String> streamActiveJtis(String userSessionsKey) {
		return Optional.ofNullable(redisTemplate.opsForSet().members(userSessionsKey))
			.orElseGet(Collections::emptySet)
			.stream()
			.filter(String.class::isInstance)
			.map(String.class::cast)
			.filter(CharSequenceUtil::isNotBlank);
	}

	/**
	 * 列表场景：一次 HGETALL；空 Hash 则清理 Set/ZSet 并返回 empty
	 * @param userSessionsKey 用户会话 Set key
	 * @param userId 用户 ID
	 * @param jti 会话唯一标识
	 * @return 会话索引
	 */
	private Optional<UserSessionIndex> loadListedSession(String userSessionsKey, long userId, String jti) {
		Map<Object, Object> hash = redisTemplate.opsForHash().entries(USER_SESSION.key(jti));
		if (CollUtil.isEmpty(hash)) {
			removeStaleIndexes(userSessionsKey, jti);
			return Optional.empty();
		}
		return Optional.of(toSessionIndex(jti, hash, userId));
	}

	/**
	 * 从 Hash 组装会话索引
	 * @param jti 会话唯一标识
	 * @param hash 会话 Hash
	 * @param overrideUserId 覆盖 userId（列表场景传入；单条加载时为 null）
	 * @return 会话索引
	 */
	private UserSessionIndex toSessionIndex(String jti, Map<Object, Object> hash, Long overrideUserId) {
		UserSessionIndex sessionIndex = BeanUtil.toBean(hash, UserSessionIndex.class);
		if (overrideUserId != null) {
			sessionIndex.setUserId(overrideUserId);
		}
		sessionIndex.setSessionId(jti);
		return sessionIndex;
	}

	/**
	 * 清理用户 Set 与全局 ZSet 中的僵尸 jti
	 * @param userSessionsKey 用户会话 Set key
	 * @param jti 会话唯一标识
	 * @return 是否移除了任一索引
	 */
	private boolean removeStaleIndexes(String userSessionsKey, String jti) {
		Long setRemoved = redisTemplate.opsForSet().remove(userSessionsKey, jti);
		Long zsetRemoved = redisTemplate.opsForZSet().remove(GLOBAL_SESSIONS.fixedKey(), jti);
		return NumberUtil.nullToZero(setRemoved) > 0 || NumberUtil.nullToZero(zsetRemoved) > 0;
	}

	/**
	 * 构建 register Lua 脚本参数
	 * @param userSessionIndex 会话索引
	 * @param sessionId 会话 ID
	 * @param loginAtScore 登录时间分值
	 * @param ttl 会话 TTL
	 * @return ARGV 数组
	 */
	private Object[] buildRegisterScriptArgs(UserSessionIndex userSessionIndex, String sessionId, double loginAtScore,
			Duration ttl) {
		List<Object> args = new ArrayList<>();
		args.add(sessionId);
		args.add(loginAtScore);
		args.add(ttl != null ? ttl.getSeconds() : 0L);

		Map<String, Object> sessionMap = BeanUtil.beanToMap(userSessionIndex);
		sessionMap.forEach((field, value) -> {
			if (value != null) {
				args.add(field);
				args.add(String.valueOf(value));
			}
		});
		return args.toArray();
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
		Long ttl = redisTemplate.getExpire(USER_SESSION.key(jti), TimeUnit.SECONDS);

		// null: Redis 未返回 TTL；-2: key 不存在；-1: 无过期都视为较新，避免误淘汰有效长会话
		if (ttl == null || ttl < 0) {
			return Long.MAX_VALUE;
		}
		return ttl;
	}

}
