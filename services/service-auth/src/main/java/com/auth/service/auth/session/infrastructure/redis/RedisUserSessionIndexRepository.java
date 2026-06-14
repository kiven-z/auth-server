package com.auth.service.auth.session.infrastructure.redis;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.session.domain.model.OnlineSessionPageSlice;
import com.auth.service.auth.session.infrastructure.redis.script.UserSessionRedisScripts;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.auth.module.security.contract.redis.SecurityRedisKey.*;

/**
 * 用户会话 Redis 索引：注册、终止、查询与僵尸索引清理
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
	 * 全局在线会话分页（按 loginAt 倒序；Hash 不存在时清理 ZSet 残留）
	 * @param pageIndex 页码（从 1 开始）
	 * @param pageSize 每页条数
	 * @param allowedUserIds 用户名筛选后的用户 ID 集合；{@code null} 表示不过滤用户
	 * @param ipAddress IP 地址关键字（包含匹配）；空白表示不过滤
	 * @return 分页切片
	 */
	public OnlineSessionPageSlice pageGlobalSessions(int pageIndex, int pageSize, Set<Long> allowedUserIds,
			String ipAddress) {
		int normalizedPageIndex = normalizePageIndex(pageIndex);
		int normalizedPageSize = normalizePageSize(pageSize);
		Predicate<UserSessionIndex> filter = buildGlobalSessionFilter(allowedUserIds, ipAddress);
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
			.filter(this::removeStaleGlobalIndex)
			.count();
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
	 * 全局列表场景：空 Hash 时清理 ZSet 与用户 Set 残留
	 * @param jti 会话唯一标识
	 * @return 会话索引
	 */
	private Optional<UserSessionIndex> loadGlobalListedSession(String jti) {
		Map<Object, Object> hash = redisTemplate.opsForHash().entries(USER_SESSION.key(jti));
		if (CollUtil.isEmpty(hash)) {
			removeStaleGlobalIndex(jti);
			return Optional.empty();
		}
		UserSessionIndex sessionIndex = toSessionIndex(jti, hash, null);
		Long userId = sessionIndex.getUserId();
		if (userId != null) {
			String userSessionsKey = USER_SESSIONS.key(userId);
			Boolean member = redisTemplate.opsForSet().isMember(userSessionsKey, jti);
			if (Boolean.FALSE.equals(member)) {
				redisTemplate.opsForSet().add(userSessionsKey, jti);
			}
		}
		return Optional.of(sessionIndex);
	}

	/**
	 * 统计符合筛选条件的全局会话数
	 * @param filter 筛选条件
	 * @return 会话总数
	 */
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
	 * 构建全局会话筛选条件
	 * @param allowedUserIds 允许的用户 ID 集合；{@code null} 表示不过滤
	 * @param ipAddress IP 地址关键字
	 * @return 筛选谓词
	 */
	private Predicate<UserSessionIndex> buildGlobalSessionFilter(Set<Long> allowedUserIds, String ipAddress) {
		String ipKeyword = CharSequenceUtil.trim(ipAddress);
		return session -> matchesUserFilter(session, allowedUserIds) && matchesIpFilter(session, ipKeyword);
	}

	/**
	 * 用户 ID 筛选
	 * @param session 会话索引
	 * @param allowedUserIds 允许的用户 ID 集合
	 * @return 是否匹配
	 */
	private boolean matchesUserFilter(UserSessionIndex session, Set<Long> allowedUserIds) {
		if (allowedUserIds == null) {
			return true;
		}
		Long userId = session.getUserId();
		return userId != null && allowedUserIds.contains(userId);
	}

	/**
	 * IP 地址包含匹配
	 * @param session 会话索引
	 * @param ipKeyword IP 关键字
	 * @return 是否匹配
	 */
	private boolean matchesIpFilter(UserSessionIndex session, String ipKeyword) {
		if (CharSequenceUtil.isBlank(ipKeyword)) {
			return true;
		}
		return CharSequenceUtil.contains(session.getIpAddress(), ipKeyword);
	}

	/**
	 * 清理全局 ZSet 僵尸 jti；若能从 Hash 解析 userId 则同步清理用户 Set
	 * @param jti 会话唯一标识
	 * @return 是否移除了任一索引
	 */
	private boolean removeStaleGlobalIndex(String jti) {
		Long zsetRemoved = redisTemplate.opsForZSet().remove(GLOBAL_SESSIONS.fixedKey(), jti);
		return NumberUtil.nullToZero(zsetRemoved) > 0;
	}

	/**
	 * 规范化页码
	 * @param pageIndex 页码
	 * @return 不小于 1 的页码
	 */
	private int normalizePageIndex(int pageIndex) {
		return Math.max(1, pageIndex);
	}

	/**
	 * 规范化分页大小
	 * @param pageSize 分页大小
	 * @return 限制在 1～{@link #MAX_PAGE_SIZE} 的分页大小
	 */
	private int normalizePageSize(int pageSize) {
		return Math.min(Math.max(1, pageSize), MAX_PAGE_SIZE);
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

	/**
	 * 全局会话扫描结果
	 *
	 * @param matchedCount 匹配会话总数
	 * @param pageRecords 当前页记录
	 */
	private record GlobalSessionScanResult(long matchedCount, List<UserSessionIndex> pageRecords) {

	}

}
