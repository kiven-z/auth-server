package com.auth.service.auth.infrastructure.redis.session;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.auth.domain.session.OnlineUserPageSlice;
import com.auth.service.auth.domain.session.OnlineUserPageSlice.OnlineUserEntry;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.LongPredicate;

import static com.auth.module.security.contract.redis.SecurityRedisKey.ONLINE_USERS;

/**
 * 在线用户 ZSet 读侧：分页扫描、统计与读时清理残留成员
 *
 * @author Bunny
 */
@Service
public class RedisOnlineUserIndexReader {

	/**
	 * 在线用户 ZSet 扫描批次大小
	 */
	private static final int ONLINE_USER_SCAN_BATCH = 64;

	/**
	 * 单页最大条数
	 */
	private static final int MAX_PAGE_SIZE = 100;

	private final UserSessionIndexStore userSessionIndexStore;

	private final RedisTemplate<String, String> sessionRedisTemplate;

	public RedisOnlineUserIndexReader(UserSessionIndexStore userSessionIndexStore,
			@Qualifier("sessionRedisTemplate") RedisTemplate<String, String> sessionRedisTemplate) {
		this.userSessionIndexStore = userSessionIndexStore;
		this.sessionRedisTemplate = sessionRedisTemplate;
	}

	/**
	 * 在线用户分页（按 lastLoginAt 倒序；无活跃会话时清理 ZSet 残留）
	 * @param pageIndex 页码（从 1 开始）
	 * @param pageSize 每页条数
	 * @param allowedUserIds 用户 ID 筛选集合；{@code null} 表示不过滤用户
	 * @return 分页切片
	 */
	public OnlineUserPageSlice pageOnlineUsers(int pageIndex, int pageSize, Set<Long> allowedUserIds) {
		int normalizedPageIndex = Math.max(1, pageIndex);
		int normalizedPageSize = Math.min(Math.max(1, pageSize), MAX_PAGE_SIZE);

		LongPredicate filter = userId -> allowedUserIds == null || allowedUserIds.contains(userId);
		OnlineUserScanResult scanResult = scanOnlineUsers(filter, normalizedPageIndex, normalizedPageSize);
		return new OnlineUserPageSlice(scanResult.matchedCount(), scanResult.pageRecords());
	}

	/**
	 * 统计在线用户 ZSet 成员数（原始 ZCARD，可能含尚未被读路径清理的残留成员）
	 * @return 在线用户数
	 */
	public long countOnlineUsers() {
		String key = ONLINE_USERS.fixedKey();
		Long zcard = sessionRedisTemplate.opsForZSet().zCard(key);

		return zcard != null ? zcard : 0L;
	}

	/**
	 * 扫描在线用户 ZSet：统计匹配总数，并收集当前页记录
	 * @param filter 筛选条件
	 * @param pageIndex 页码
	 * @param pageSize 每页条数
	 * @return 扫描结果
	 */
	@NotNull
	private OnlineUserScanResult scanOnlineUsers(LongPredicate filter, int pageIndex, int pageSize) {
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
	private long collectOnlineUserBatch(Set<String> members, LongPredicate filter, OnlineUserPageCollector collector) {
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
	private int tryCollectOnlineUser(String userIdText, LongPredicate filter, OnlineUserPageCollector collector) {
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

		long sessionCount = userSessionIndexStore.countActiveSessions(userId);
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
