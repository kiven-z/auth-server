package com.auth.service.auth.application.session;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.application.port.SessionRevocationPort;
import com.auth.service.auth.application.port.UserBriefQueryPort;
import com.auth.service.auth.config.OnlineSessionProperties;
import com.auth.service.auth.convert.OnlineSessionPageConverter;
import com.auth.service.auth.domain.session.model.OnlineSessionPageSlice;
import com.auth.service.auth.domain.session.model.UserOnlineBrief;
import com.auth.service.auth.domain.session.model.query.OnlineSessionPageQuery;
import com.auth.service.auth.domain.session.model.vo.OnlineSessionPageVO;
import com.auth.service.auth.domain.session.model.vo.OnlineSessionSummaryVO;
import com.auth.service.auth.infrastructure.redis.session.RedisUserSessionIndexRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理端会话用例：踢人、按用户查会话、全局在线分页
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SessionAdminApplicationService implements SessionRevocationPort {

	private final RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	private final UserBriefQueryPort userBriefQueryPort;

	private final OnlineSessionProperties onlineSessionProperties;

	/**
	 * 踢出指定会话
	 * @param userId 用户 ID
	 * @param sessionId 会话 ID（jti）
	 */
	@Override
	public void kickSession(long userId, String sessionId) {
		redisUserSessionIndexRepository.terminateSession(userId, sessionId);
	}

	/**
	 * 踢出用户全部会话
	 * @param userId 用户 ID
	 */
	@Override
	public void kickAllSessions(long userId) {
		redisUserSessionIndexRepository.terminateAllSessions(userId);
	}

	/**
	 * 批量踢出用户全部会话（跳过 null，单用户失败不影响其余用户）
	 * @param userIds 用户 ID 列表
	 */
	@Override
	public void kickAllSessions(Collection<Long> userIds) {
		userIds.stream().filter(Objects::nonNull).forEach(userId -> {
			try {
				kickAllSessions(userId);
			}
			catch (RuntimeException ex) {
				log.warn("Failed to kick all sessions for userId={}: {}", userId, ex.getMessage());
			}
		});
	}

	/**
	 * 查询用户活跃会话列表
	 * @param userId 用户 ID
	 * @return 活跃会话列表
	 */
	public List<UserSessionIndex> listActiveSessions(long userId) {
		return redisUserSessionIndexRepository.listUserSessions(userId);
	}

	/**
	 * 分页查询全局在线会话
	 * @param query 查询条件
	 * @return 分页结果
	 */
	@Transactional(readOnly = true)
	public PageResponse<OnlineSessionPageVO> getOnlineSessionPage(OnlineSessionPageQuery query) {
		redisUserSessionIndexRepository
			.cleanupStaleGlobalSessions(onlineSessionProperties.getQuery().getPreCleanupBatch());

		int pageIndex = Objects.requireNonNullElse(query.getPageIndex(), 1);
		int pageSize = Objects.requireNonNullElse(query.getPageSize(), 30);

		Set<Long> allowedUserIds = null;
		if (CharSequenceUtil.isNotBlank(query.getUsername())) {
			List<Long> matchedUserIds = userBriefQueryPort.findIdsByUsernameLike(query.getUsername().trim());
			if (CollUtil.isEmpty(matchedUserIds)) {
				return PageResponse.of((long) pageIndex, (long) pageSize, 0L, List.of());
			}
			allowedUserIds = Set.copyOf(matchedUserIds);
		}

		OnlineSessionPageSlice slice = redisUserSessionIndexRepository.pageGlobalSessions(pageIndex, pageSize,
				allowedUserIds, query.getIpAddress());
		Set<Long> userIds = slice.sessions()
			.stream()
			.map(UserSessionIndex::getUserId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		Map<Long, UserOnlineBrief> briefMap = userBriefQueryPort.findByIds(userIds);
		List<OnlineSessionPageVO> records = slice.sessions()
			.stream()
			.map(session -> OnlineSessionPageConverter.INSTANCE.toPageVo(session, briefMap.get(session.getUserId())))
			.toList();
		return PageResponse.of((long) pageIndex, (long) pageSize, slice.total(), records);
	}

	/**
	 * 全局在线会话统计摘要
	 * @return 在线会话索引条数
	 */
	@Transactional(readOnly = true)
	public OnlineSessionSummaryVO getOnlineSessionSummary() {
		OnlineSessionSummaryVO summary = new OnlineSessionSummaryVO();
		summary.setSessionCount(redisUserSessionIndexRepository.countGlobalSessionIndex());
		return summary;
	}

	/**
	 * 后台 reconcile：循环清理 global ZSet 中 Hash 已不存在的僵尸 jti，直到本批无清理或达到 maxRounds
	 * @return 本次累计清理条数
	 */
	public int reconcileStaleGlobalSessions() {
		OnlineSessionProperties.Reconcile reconcile = onlineSessionProperties.getReconcile();
		int batchSize = reconcile.getBatchSize();
		int maxRounds = reconcile.getMaxRounds();
		int totalCleaned = 0;
		for (int round = 0; round < maxRounds; round++) {
			int cleaned = redisUserSessionIndexRepository.cleanupStaleGlobalSessions(batchSize);
			totalCleaned += cleaned;
			if (cleaned <= 0) {
				break;
			}
		}
		return totalCleaned;
	}

}
