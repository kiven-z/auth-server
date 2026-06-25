package com.auth.service.auth.infrastructure.redis.session;

import java.util.Objects;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.NumberUtil;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.infrastructure.redis.session.support.UserSessionIndexRedisCodec;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.auth.module.security.contract.redis.SecurityRedisKey.*;

/**
 * 单用户会话索引：计数、列表、僵尸清理与超限淘汰
 *
 * @author Bunny
 */
@Service
public class UserSessionIndexStore {

	private final RedisTemplate<String, String> sessionRedisTemplate;

	private final UserSessionIndexRedisCodec userSessionIndexRedisCodec;

	private final UserSessionLifecycleStore userSessionLifecycleStore;

	public UserSessionIndexStore(@Qualifier("sessionRedisTemplate") RedisTemplate<String, String> sessionRedisTemplate,
			UserSessionIndexRedisCodec userSessionIndexRedisCodec,
			UserSessionLifecycleStore userSessionLifecycleStore) {
		this.sessionRedisTemplate = sessionRedisTemplate;
		this.userSessionIndexRedisCodec = userSessionIndexRedisCodec;
		this.userSessionLifecycleStore = userSessionLifecycleStore;
	}

	/**
	 * 统计用户活跃会话数
	 * @param userId 用户ID
	 * @return 活跃会话数
	 */
	public long countActiveSessions(long userId) {
		return Objects.requireNonNullElse(sessionRedisTemplate.opsForSet().size(USER_SESSIONS.key(userId)), 0L);
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
			.filter(jti -> CollUtil.isEmpty(userSessionIndexRedisCodec
				.asStringHash(sessionRedisTemplate.opsForHash().entries(USER_SESSION.key(jti)))))
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
			.min(Comparator.comparingLong(jti -> {
				Long ttl = sessionRedisTemplate.getExpire(USER_SESSION.key(jti), TimeUnit.SECONDS);
				// null / -2 / -1 视为较新，避免误淘汰有效长会话
				if (ttl == null || ttl < 0) {
					return Long.MAX_VALUE;
				}
				return ttl;
			}))
			.map(evictedJti -> {
				userSessionLifecycleStore.terminateSession(userId, evictedJti);
				return evictedJti;
			});
	}

	/**
	 * 用户活跃会话 Set 中的 jti 流
	 * @param userSessionsKey 用户会话 Set key
	 * @return 非空 jti 流
	 */
	private Stream<String> streamActiveJtis(String userSessionsKey) {
		return Optional.ofNullable(sessionRedisTemplate.opsForSet().members(userSessionsKey))
			.orElseGet(Collections::emptySet)
			.stream()
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
		Map<String, String> hash = userSessionIndexRedisCodec
			.asStringHash(sessionRedisTemplate.opsForHash().entries(USER_SESSION.key(jti)));
		if (CollUtil.isEmpty(hash)) {
			removeStaleIndexes(userSessionsKey, jti);
			return Optional.empty();
		}
		return Optional.of(userSessionIndexRedisCodec.toSessionIndex(jti, hash, userId));
	}

	/**
	 * 清理用户 Set 中的僵尸 jti
	 * @param userSessionsKey 用户会话 Set key
	 * @param jti 会话唯一标识
	 * @return 是否移除了 Set 成员
	 */
	private boolean removeStaleIndexes(String userSessionsKey, String jti) {
		Long setRemoved = sessionRedisTemplate.opsForSet().remove(userSessionsKey, jti);
		return NumberUtil.nullToZero(setRemoved) > 0;
	}

}
