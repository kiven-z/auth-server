package com.auth.service.auth.application.session;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.application.port.SessionRevocationPort;
import com.auth.service.auth.application.port.UserBriefQueryPort;
import com.auth.service.auth.domain.session.model.OnlineUserPageSlice;
import com.auth.service.auth.domain.session.model.OnlineUserPageSlice.OnlineUserEntry;
import com.auth.service.auth.domain.session.model.UserOnlineBrief;
import com.auth.service.auth.domain.session.model.query.OnlineUserPageQuery;
import com.auth.service.auth.domain.session.model.vo.OnlineUserPageVO;
import com.auth.service.auth.domain.session.model.vo.OnlineUserSummaryVO;
import com.auth.service.auth.infrastructure.redis.session.RedisUserSessionIndexRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理端会话用例：踢人、按用户查会话、在线用户分页
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SessionAdminApplicationService implements SessionRevocationPort {

	private final RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	private final UserBriefQueryPort userBriefQueryPort;

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
	 * 分页查询在线用户
	 * @param query 查询条件
	 * @return 分页结果
	 */
	@Transactional(readOnly = true)
	public PageResponse<OnlineUserPageVO> getOnlineUserPage(OnlineUserPageQuery query) {
		// 默认分页
		int pageIndex = Objects.requireNonNullElse(query.getPageIndex(), 1);
		int pageSize = Objects.requireNonNullElse(query.getPageSize(), 30);

		Set<Long> allowedUserIds = null;
		if (CharSequenceUtil.isNotBlank(query.getUsername())) {
			List<Long> matchedUserIds = userBriefQueryPort.findIdsByUsernameLike(query.getUsername());
			if (CollUtil.isEmpty(matchedUserIds)) {
				return PageResponse.of((long) pageIndex, (long) pageSize, 0L, List.of());
			}
			allowedUserIds = Set.copyOf(matchedUserIds);
		}

		OnlineUserPageSlice slice = redisUserSessionIndexRepository.pageOnlineUsers(pageIndex, pageSize,
				allowedUserIds);
		Set<Long> userIds = slice.users().stream().map(OnlineUserEntry::userId).collect(Collectors.toSet());
		Map<Long, UserOnlineBrief> briefMap = userBriefQueryPort.findByIds(userIds);

		// 组装在线用户分页行
		List<OnlineUserPageVO> records = slice.users().stream().map(entry -> {
			OnlineUserPageVO vo = new OnlineUserPageVO();
			vo.setUserId(entry.userId());
			vo.setActiveSessionCount(entry.activeSessionCount());
			vo.setLastLoginAt(entry.lastLoginAt());
			UserOnlineBrief brief = briefMap.get(entry.userId());
			if (brief != null) {
				vo.setUsername(brief.username());
				vo.setNickname(brief.nickname());
			}
			return vo;
		}).toList();
		return PageResponse.of((long) pageIndex, (long) pageSize, slice.total(), records);
	}

	/**
	 * 在线用户统计摘要
	 * @return 在线用户数
	 */
	@Transactional(readOnly = true)
	public OnlineUserSummaryVO getOnlineUserSummary() {
		long onlineUserCount = redisUserSessionIndexRepository.countOnlineUsers();

		OnlineUserSummaryVO summary = new OnlineUserSummaryVO();
		summary.setOnlineUserCount(onlineUserCount);
		return summary;
	}

}
