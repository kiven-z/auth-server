package com.auth.service.auth.infrastructure.redis.session.adapter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.auth.application.port.session.OnlineUserQueryPort;
import com.auth.service.auth.domain.session.model.OnlineUserPageSlice;
import com.auth.service.auth.domain.session.model.OnlineUserPageSlice.OnlineUserEntry;
import com.auth.service.auth.infrastructure.redis.session.UserSessionIndexStore;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.LongPredicate;

import static com.auth.module.security.contract.redis.SecurityRedisKey.ONLINE_USERS;

/**
 * 在线用户 ZSet 只读查询适配器：分页扫描、统计与读时清理残留成员。
 *
 * @author Bunny
 */
@Repository
public class OnlineUserQueryAdapter implements OnlineUserQueryPort {

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

	public OnlineUserQueryAdapter(UserSessionIndexStore userSessionIndexStore,
			@Qualifier("sessionRedisTemplate") RedisTemplate<String, String> sessionRedisTemplate) {
		this.userSessionIndexStore = userSessionIndexStore;
		this.sessionRedisTemplate = sessionRedisTemplate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OnlineUserPageSlice pageOnlineUsers(int pageIndex, int pageSize, Set<Long> allowedUserIds) {
		int normalizedPageIndex = Math.max(1, pageIndex);
		int normalizedPageSize = Math.min(Math.max(1, pageSize), MAX_PAGE_SIZE);

		LongPredicate filter = userId -> allowedUserIds == null || allowedUserIds.contains(userId);
		OnlineUserScanResult scanResult = scanOnlineUsers(filter, normalizedPageIndex, normalizedPageSize);
		return new OnlineUserPageSlice(scanResult.matchedCount(), scanResult.pageRecords());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long countOnlineUsers() {
		String key = ONLINE_USERS.fixedKey();
		Long zcard = sessionRedisTemplate.opsForZSet().zCard(key);

		return zcard != null ? zcard : 0L;
	}

	/**
	 * 扫描在线用户
	 * @param filter 过滤器
	 * @param pageIndex 页索引
	 * @param pageSize 页大小
	 * @return 扫描结果
	 */
	@NotNull
	private OnlineUserScanResult scanOnlineUsers(LongPredicate filter, int pageIndex, int pageSize) {
		// 计算跳过数量
		int toSkip = (pageIndex - 1) * pageSize;
		List<OnlineUserEntry> pageRecords = new ArrayList<>(pageSize);

		// 获取在线用户数量
		Long zcard = sessionRedisTemplate.opsForZSet().zCard(ONLINE_USERS.fixedKey());
		if (zcard == null || zcard <= 0) {
			return new OnlineUserScanResult(0L, pageRecords);
		}

		// 创建收集器
		OnlineUserPageCollector collector = new OnlineUserPageCollector(toSkip, pageSize, pageRecords);
		long matchedCount = 0L;
		long rank = 0L;

		// 扫描在线用户
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
	 * 收集在线用户批次
	 * @param members 成员
	 * @param filter 过滤器
	 * @param collector 收集器
	 * @return 匹配数量
	 */
	private long collectOnlineUserBatch(Set<String> members, LongPredicate filter, OnlineUserPageCollector collector) {
		long batchMatched = 0L;
		for (String userIdText : members) {
			batchMatched += tryCollectOnlineUser(userIdText, filter, collector);
		}
		return batchMatched;
	}

	/**
	 * 尝试收集在线用户
	 * @param userIdText 用户 ID
	 * @param filter 过滤器
	 * @param collector 收集器
	 * @return 收集结果
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
	 * 加载在线用户条目
	 * @param userIdText 用户 ID
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

		// 清理僵尸会话
		userSessionIndexStore.cleanupStaleActiveSessions(userId);

		// 获取活跃会话数量
		long sessionCount = userSessionIndexStore.countActiveSessions(userId);
		if (sessionCount <= 0) {
			sessionRedisTemplate.opsForZSet().remove(ONLINE_USERS.fixedKey(), userIdText);
			return Optional.empty();
		}

		// 获取登录时间
		Double score = sessionRedisTemplate.opsForZSet().score(ONLINE_USERS.fixedKey(), userIdText);
		long lastLoginAt = score != null ? score.longValue() : 0L;
		return Optional.of(new OnlineUserEntry(userId, lastLoginAt, (int) sessionCount));
	}

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

	private record OnlineUserScanResult(long matchedCount, List<OnlineUserEntry> pageRecords) {

	}

}
