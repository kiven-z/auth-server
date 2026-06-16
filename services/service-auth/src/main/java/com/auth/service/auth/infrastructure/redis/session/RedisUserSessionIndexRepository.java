package com.auth.service.auth.infrastructure.redis.session;

import cn.hutool.core.util.NumberUtil;
import com.auth.module.security.contract.api.UserSessionIndex;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static com.auth.module.security.contract.redis.SecurityRedisKey.ONLINE_USERS;
import static com.auth.module.security.contract.redis.SecurityRedisKey.USER_SESSIONS;

/**
 * 用户会话 Redis 索引：生命周期与单用户索引维护
 *
 * @author Bunny
 */
@Service
public class RedisUserSessionIndexRepository {

	private final UserSessionLifecycleStore userSessionLifecycleStore;

	private final UserSessionIndexStore userSessionIndexStore;

	private final RedisTemplate<String, String> sessionRedisTemplate;

	public RedisUserSessionIndexRepository(UserSessionLifecycleStore userSessionLifecycleStore,
			UserSessionIndexStore userSessionIndexStore,
			@Qualifier("sessionRedisTemplate") RedisTemplate<String, String> sessionRedisTemplate) {
		this.userSessionLifecycleStore = userSessionLifecycleStore;
		this.userSessionIndexStore = userSessionIndexStore;
		this.sessionRedisTemplate = sessionRedisTemplate;
	}

	/**
	 * 统计用户活跃会话数
	 * @param userId 用户ID
	 * @return 活跃会话数
	 */
	public long countActiveSessions(long userId) {
		return userSessionIndexStore.countActiveSessions(userId);
	}

	/**
	 * 注册活跃会话：写入 Hash、用户 Set 与在线用户 ZSet（Lua 原子）
	 * @param userSessionIndex 用户会话索引
	 */
	public void registerSession(@NotNull UserSessionIndex userSessionIndex) {
		userSessionLifecycleStore.registerSession(userSessionIndex);
	}

	/**
	 * 终止单条会话并在无活跃会话时移出在线用户索引
	 * @param userId 用户 ID
	 * @param jti 会话唯一标识
	 */
	public void terminateSession(long userId, @NotNull String jti) {
		userSessionLifecycleStore.terminateSession(userId, jti);
		syncOnlineUserAfterTerminate(userId);
	}

	/**
	 * 终止用户全部活跃会话并移出在线用户索引
	 * @param userId 用户 ID
	 */
	public void terminateAllSessions(long userId) {
		userSessionLifecycleStore.terminateAllSessions(userId);
		syncOnlineUserAfterTerminate(userId);
	}

	/**
	 * 读取会话索引（UserSessionIndex）（不存在返回空）
	 * @param jti 会话唯一标识
	 * @return 会话索引
	 */
	public Optional<UserSessionIndex> loadUserSessionIndex(@NotNull String jti) {
		return userSessionLifecycleStore.loadUserSessionIndex(jti);
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
		userSessionLifecycleStore.updateRefreshInfo(jti, refreshTokenHash, refreshTokenExpiresAt, ttl);
	}

	/**
	 * 获取用户所有活跃会话（Hash 不存在时清理 Set 残留）
	 * @param userId 用户ID
	 * @return 会话列表
	 */
	public List<UserSessionIndex> listUserSessions(long userId) {
		List<UserSessionIndex> sessions = userSessionIndexStore.listUserSessions(userId);
		syncOnlineUserAfterTerminate(userId);
		return sessions;
	}

	/**
	 * 会话终止后：若无活跃会话则从在线用户 ZSet 移除
	 * @param userId 用户 ID
	 */
	public void syncOnlineUserAfterTerminate(long userId) {
		Long sessionCount = sessionRedisTemplate.opsForSet().size(USER_SESSIONS.key(userId));

		if (NumberUtil.nullToZero(sessionCount) <= 0) {
			String key = ONLINE_USERS.fixedKey();
			String value = String.valueOf(userId);

			sessionRedisTemplate.opsForZSet().remove(key, value);
		}
	}

	/**
	 * 清理活跃会话集合中的僵尸会话（session 详情已不存在）
	 * @param userId 用户ID
	 * @return 清理数量
	 */
	public int cleanupStaleActiveSessions(long userId) {
		int cleaned = userSessionIndexStore.cleanupStaleActiveSessions(userId);

		syncOnlineUserAfterTerminate(userId);
		return cleaned;
	}

	/**
	 * 超限时淘汰最旧会话并返回被淘汰的会话ID
	 * @param userId 用户ID
	 * @return 被淘汰会话ID，若无可淘汰会话则为空
	 */
	public Optional<String> evictOldestActiveSession(long userId) {
		Optional<String> evicted = userSessionIndexStore.evictOldestActiveSession(userId);

		evicted.ifPresent(jti -> syncOnlineUserAfterTerminate(userId));
		return evicted;
	}

	/**
	 * 从 Hash 解析 userId 后终止；Hash 已丢失时仍尝试清理在线用户索引
	 * @param jti 会话唯一标识
	 */
	public void terminateSession(@NotNull String jti) {
		Optional<UserSessionIndex> sessionIndex = loadUserSessionIndex(jti);
		userSessionLifecycleStore.terminateSession(jti);

		sessionIndex.map(UserSessionIndex::getUserId).ifPresent(this::syncOnlineUserAfterTerminate);
	}

}
