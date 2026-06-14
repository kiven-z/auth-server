package com.auth.service.auth.session.infrastructure.redis;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.NumberUtil;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.session.domain.model.OnlineSessionPageSlice;
import com.auth.service.auth.session.infrastructure.redis.support.UserSessionIndexRedisCodec;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

import static com.auth.module.security.contract.redis.SecurityRedisKey.GLOBAL_SESSIONS;
import static com.auth.module.security.contract.redis.SecurityRedisKey.USER_SESSION;

/**
 * 用户会话 Redis 索引：生命周期、单用户索引与全局在线分页
 *
 * @author Bunny
 */
@Service
public class RedisUserSessionIndexRepository {

	/**
	 * 全局 ZSet 扫描批次大小
	 */
	private static final int GLOBAL_SCAN_BATCH = 64;

	/**
	 * 单页最大条数
	 */
	private static final int MAX_PAGE_SIZE = 100;

	private final UserSessionLifecycleStore userSessionLifecycleStore;

	private final UserSessionIndexStore userSessionIndexStore;

	private final RedisTemplate<String, Object> redisTemplate;

	private final UserSessionIndexRedisCodec userSessionIndexRedisCodec;

	public RedisUserSessionIndexRepository(UserSessionLifecycleStore userSessionLifecycleStore,
			UserSessionIndexStore userSessionIndexStore, RedisTemplate<String, Object> redisTemplate,
			UserSessionIndexRedisCodec userSessionIndexRedisCodec) {
		this.userSessionLifecycleStore = userSessionLifecycleStore;
		this.userSessionIndexStore = userSessionIndexStore;
		this.redisTemplate = redisTemplate;
		this.userSessionIndexRedisCodec = userSessionIndexRedisCodec;
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
	 * 注册活跃会话：写入 Hash、用户 Set 与全局 ZSet（Lua 原子）
	 * @param userSessionIndex 用户会话索引
	 */
	public void registerSession(@NotNull UserSessionIndex userSessionIndex) {
		userSessionLifecycleStore.registerSession(userSessionIndex);
	}

	/**
	 * 终止单条会话：删除 Hash、用户 Set 与全局 ZSet（Lua 原子，幂等）
	 * @param userId 用户 ID
	 * @param jti 会话唯一标识
	 */
	public void terminateSession(long userId, @NotNull String jti) {
		userSessionLifecycleStore.terminateSession(userId, jti);
	}

	/**
	 * 终止用户全部活跃会话
	 * @param userId 用户 ID
	 */
	public void terminateAllSessions(long userId) {
		userSessionLifecycleStore.terminateAllSessions(userId);
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
	 * 获取用户所有活跃会话（Hash 不存在时清理 Set/ZSet 残留）
	 * @param userId 用户ID
	 * @return 会话列表
	 */
	public List<UserSessionIndex> listUserSessions(long userId) {
		return userSessionIndexStore.listUserSessions(userId);
	}

	/**
	 * 全局在线会话分页（按 loginAt 倒序；Hash 不存在时清理 ZSet 残留）
	 * @param pageIndex 页码（从 1 开始）
	 * @param pageSize 每页条数
	 * @param allowedUserIds 用户名筛选后的用户 ID 集合；{@code null} 表示不过滤用户
	 * @param ipAddress IP 地址关键字（包含匹配）；空白表示不过滤
	 * @return 分页切片
	 */
	public OnlineSessionPageSlice pageGlobalSessions(int pageIndex, int pageSize, Set<Long> allowedUserIds,
			String ipAddress) {
		int normalizedPageIndex = Math.max(1, pageIndex);
		int normalizedPageSize = Math.min(Math.max(1, pageSize), MAX_PAGE_SIZE);
		String ipKeyword = CharSequenceUtil.trim(ipAddress);
		Predicate<UserSessionIndex> filter = session -> {
			if (allowedUserIds != null) {
				Long userId = session.getUserId();
				if (userId == null || !allowedUserIds.contains(userId)) {
					return false;
				}
			}
			return CharSequenceUtil.isBlank(ipKeyword) || CharSequenceUtil.contains(session.getIpAddress(), ipKeyword);
		};
		GlobalSessionScanResult scanResult = scanGlobalSessions(filter, normalizedPageIndex, normalizedPageSize);
		return new OnlineSessionPageSlice(scanResult.matchedCount(), scanResult.pageRecords());
	}

	/**
	 * 清理全局 ZSet 中 Hash 已不存在的僵尸 jti
	 * @param batchSize 单批扫描条数
	 * @return 本批清理数量
	 */
	public int cleanupStaleGlobalSessions(int batchSize) {
		int normalizedBatch = Math.max(1, batchSize);
		Set<Object> members = redisTemplate.opsForZSet()
			.reverseRange(GLOBAL_SESSIONS.fixedKey(), 0, normalizedBatch - 1L);
		if (CollUtil.isEmpty(members)) {
			return 0;
		}
		return (int) members.stream()
			.filter(String.class::isInstance)
			.map(String.class::cast)
			.filter(jti -> CollUtil.isEmpty(redisTemplate.opsForHash().entries(USER_SESSION.key(jti))))
			.filter(jti -> NumberUtil
				.nullToZero(redisTemplate.opsForZSet().remove(GLOBAL_SESSIONS.fixedKey(), jti)) > 0)
			.count();
	}

	/**
	 * 清理活跃会话集合中的僵尸会话（session 详情已不存在）
	 * @param userId 用户ID
	 * @return 清理数量
	 */
	public int cleanupStaleActiveSessions(long userId) {
		return userSessionIndexStore.cleanupStaleActiveSessions(userId);
	}

	/**
	 * 超限时淘汰最旧会话并返回被淘汰的会话ID
	 * @param userId 用户ID
	 * @return 被淘汰会话ID，若无可淘汰会话则为空
	 */
	public Optional<String> evictOldestActiveSession(long userId) {
		return userSessionIndexStore.evictOldestActiveSession(userId);
	}

	/**
	 * 从 Hash 解析 userId 后终止；Hash 已丢失时仍清理全局 ZSet
	 * @param jti 会话唯一标识
	 */
	public void terminateSession(@NotNull String jti) {
		userSessionLifecycleStore.terminateSession(jti);
	}

	/**
	 * 扫描全局 ZSet：统计匹配总数，并收集当前页记录
	 * @param filter 筛选条件
	 * @param pageIndex 页码
	 * @param pageSize 每页条数
	 * @return 扫描结果
	 */
	private GlobalSessionScanResult scanGlobalSessions(Predicate<UserSessionIndex> filter, int pageIndex,
			int pageSize) {
		int toSkip = (pageIndex - 1) * pageSize;
		List<UserSessionIndex> pageRecords = new ArrayList<>(pageSize);
		long matchedCount = 0L;
		int skipped = 0;
		long rank = 0L;
		Long zcard = redisTemplate.opsForZSet().zCard(GLOBAL_SESSIONS.fixedKey());
		if (zcard == null || zcard <= 0) {
			return new GlobalSessionScanResult(0L, pageRecords);
		}

		while (rank < zcard) {
			Set<Object> members = redisTemplate.opsForZSet()
				.reverseRange(GLOBAL_SESSIONS.fixedKey(), rank, rank + GLOBAL_SCAN_BATCH - 1L);
			if (CollUtil.isEmpty(members)) {
				break;
			}
			for (Object member : members) {
				if (!(member instanceof String jti) || CharSequenceUtil.isBlank(jti)) {
					continue;
				}
				Optional<UserSessionIndex> session = loadGlobalListedSession(jti);
				if (session.isEmpty() || !filter.test(session.get())) {
					continue;
				}
				matchedCount++;
				if (skipped < toSkip) {
					skipped++;
					continue;
				}
				if (pageRecords.size() < pageSize) {
					pageRecords.add(session.get());
				}
			}
			rank += GLOBAL_SCAN_BATCH;
		}
		return new GlobalSessionScanResult(matchedCount, pageRecords);
	}

	/**
	 * 全局列表场景：空 Hash 时仅清理 ZSet 残留，不反向补写用户 Set
	 * @param jti 会话唯一标识
	 * @return 会话索引
	 */
	private Optional<UserSessionIndex> loadGlobalListedSession(String jti) {
		Map<Object, Object> hash = redisTemplate.opsForHash().entries(USER_SESSION.key(jti));
		if (CollUtil.isEmpty(hash)) {
			redisTemplate.opsForZSet().remove(GLOBAL_SESSIONS.fixedKey(), jti);
			return Optional.empty();
		}
		return Optional.of(userSessionIndexRedisCodec.toSessionIndex(jti, hash, null));
	}

	/**
	 * 全局会话扫描结果
	 *
	 * @param matchedCount 匹配会话总数
	 * @param pageRecords 当前页记录
	 */
	private record GlobalSessionScanResult(long matchedCount, List<UserSessionIndex> pageRecords) {

	}

}
