package com.auth.service.auth.infrastructure.redis.session;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.NumberUtil;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.domain.session.model.OnlineUserPageSlice;
import com.auth.service.auth.domain.session.model.OnlineUserPageSlice.OnlineUserEntry;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static com.auth.module.security.contract.redis.SecurityRedisKey.ONLINE_USERS;
import static com.auth.module.security.contract.redis.SecurityRedisKey.USER_SESSIONS;

/**
 * 用户会话 Redis 索引：生命周期、单用户索引与在线用户分页
 *
 * @author Bunny
 */
@Service
public class RedisUserSessionIndexRepository {

	/**
	 * 在线用户 ZSet 扫描批次大小
	 */
	private static final int ONLINE_USER_SCAN_BATCH = 64;

	/**
	 * 单页最大条数
	 */
	private static final int MAX_PAGE_SIZE = 100;

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
	 * 在线用户分页（按 lastLoginAt 倒序；无活跃会话时清理 ZSet 残留）
	 * @param pageIndex 页码（从 1 开始）
	 * @param pageSize 每页条数
	 * @param allowedUserIds 用户名筛选后的用户 ID 集合；{@code null} 表示不过滤用户
	 * @return 分页切片
	 */
	public OnlineUserPageSlice pageOnlineUsers(int pageIndex, int pageSize, Set<Long> allowedUserIds) {
		int normalizedPageIndex = Math.max(1, pageIndex);
		int normalizedPageSize = Math.min(Math.max(1, pageSize), MAX_PAGE_SIZE);
		Predicate<Long> filter = userId -> allowedUserIds == null || allowedUserIds.contains(userId);
		OnlineUserScanResult scanResult = scanOnlineUsers(filter, normalizedPageIndex, normalizedPageSize);
		return new OnlineUserPageSlice(scanResult.matchedCount(), scanResult.pageRecords());
	}

	/**
	 * 统计在线用户 ZSet 成员数
	 * @return 在线用户数
	 */
	public long countOnlineUsers() {
		Long zcard = sessionRedisTemplate.opsForZSet().zCard(ONLINE_USERS.fixedKey());
		return zcard != null ? zcard : 0L;
	}

	/**
	 * 会话终止后：若无活跃会话则从在线用户 ZSet 移除
	 * @param userId 用户 ID
	 */
	public void syncOnlineUserAfterTerminate(long userId) {
		Long sessionCount = sessionRedisTemplate.opsForSet().size(USER_SESSIONS.key(userId));
		if (NumberUtil.nullToZero(sessionCount) <= 0) {
			sessionRedisTemplate.opsForZSet().remove(ONLINE_USERS.fixedKey(), String.valueOf(userId));
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

	/**
	 * 扫描在线用户 ZSet：统计匹配总数，并收集当前页记录
	 * @param filter 筛选条件
	 * @param pageIndex 页码
	 * @param pageSize 每页条数
	 * @return 扫描结果
	 */
	private OnlineUserScanResult scanOnlineUsers(Predicate<Long> filter, int pageIndex, int pageSize) {
		int toSkip = (pageIndex - 1) * pageSize;
		List<OnlineUserEntry> pageRecords = new ArrayList<>(pageSize);
		Long zcard = sessionRedisTemplate.opsForZSet().zCard(ONLINE_USERS.fixedKey());
		if (zcard == null || zcard <= 0) {
			return new OnlineUserScanResult(0L, pageRecords);
		}

		OnlineUserPageCollector collector = new OnlineUserPageCollector(toSkip, pageSize, pageRecords);
		long matchedCount = 0L;
		long rank = 0L;
		while (rank < zcard) {
			Set<String> members = sessionRedisTemplate.opsForZSet()
				.reverseRange(ONLINE_USERS.fixedKey(), rank, rank + ONLINE_USER_SCAN_BATCH - 1L);
			if (CollUtil.isEmpty(members)) {
				break;
			}
			matchedCount += collectOnlineUserBatch(members, filter, collector);
			rank += ONLINE_USER_SCAN_BATCH;
		}
		return new OnlineUserScanResult(matchedCount, pageRecords);
	}

	/**
	 * 处理一批 ZSet 成员，累计匹配数并写入分页收集器
	 * @param members 批次成员
	 * @param filter 筛选条件
	 * @param collector 分页收集器
	 * @return 本批匹配数
	 */
	private long collectOnlineUserBatch(Set<String> members, Predicate<Long> filter,
			OnlineUserPageCollector collector) {
		long batchMatched = 0L;
		for (String userIdText : members) {
			batchMatched += tryCollectOnlineUser(userIdText, filter, collector);
		}
		return batchMatched;
	}

	/**
	 * 单条在线用户：通过筛选则计入匹配并可能加入当前页
	 * @param userIdText 用户 ID 字符串
	 * @param filter 筛选条件
	 * @param collector 分页收集器
	 * @return 是否匹配（0 或 1）
	 */
	private int tryCollectOnlineUser(String userIdText, Predicate<Long> filter, OnlineUserPageCollector collector) {
		if (CharSequenceUtil.isBlank(userIdText)) {
			return 0;
		}
		Optional<OnlineUserEntry> entry = loadOnlineUserEntry(userIdText);
		if (entry.isEmpty() || !filter.test(entry.get().userId())) {
			return 0;
		}
		collector.accept(entry.get());
		return 1;
	}

	/**
	 * 加载在线用户条目；无活跃会话时清理 ZSet 残留
	 * @param userIdText 用户 ID 字符串
	 * @return 在线用户条目
	 */
	private Optional<OnlineUserEntry> loadOnlineUserEntry(String userIdText) {
		long userId;
		try {
			userId = Long.parseLong(userIdText);
		}
		catch (NumberFormatException ex) {
			sessionRedisTemplate.opsForZSet().remove(ONLINE_USERS.fixedKey(), userIdText);
			return Optional.empty();
		}
		long sessionCount = countActiveSessions(userId);
		if (sessionCount <= 0) {
			sessionRedisTemplate.opsForZSet().remove(ONLINE_USERS.fixedKey(), userIdText);
			return Optional.empty();
		}
		Double score = sessionRedisTemplate.opsForZSet().score(ONLINE_USERS.fixedKey(), userIdText);
		long lastLoginAt = score != null ? score.longValue() : 0L;
		return Optional.of(new OnlineUserEntry(userId, lastLoginAt, (int) sessionCount));
	}

	/**
	 * 在线用户分页收集器：跳过前 N 条匹配记录后填充当前页
	 */
	private static final class OnlineUserPageCollector {

		private final int toSkip;

		private final int pageSize;

		private final List<OnlineUserEntry> pageRecords;

		private int skipped;

		private OnlineUserPageCollector(int toSkip, int pageSize, List<OnlineUserEntry> pageRecords) {
			this.toSkip = toSkip;
			this.pageSize = pageSize;
			this.pageRecords = pageRecords;
		}

		private void accept(OnlineUserEntry entry) {
			if (skipped < toSkip) {
				skipped++;
				return;
			}
			if (pageRecords.size() < pageSize) {
				pageRecords.add(entry);
			}
		}

	}

	/**
	 * 在线用户扫描结果
	 *
	 * @param matchedCount 匹配用户总数
	 * @param pageRecords 当前页记录
	 */
	private record OnlineUserScanResult(long matchedCount, List<OnlineUserEntry> pageRecords) {

	}

}
