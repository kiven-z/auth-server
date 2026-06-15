package com.auth.service.auth.application.session;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.auth.application.port.UserBriefQueryPort;
import com.auth.service.auth.application.port.session.OnlineUserQueryPort;
import com.auth.service.auth.domain.session.model.OnlineUserPageSlice;
import com.auth.service.auth.domain.session.model.OnlineUserPageSlice.OnlineUserEntry;
import com.auth.service.auth.domain.session.model.UserOnlineBrief;
import com.auth.service.auth.domain.session.model.query.OnlineUserPageQuery;
import com.auth.service.auth.domain.session.model.vo.OnlineUserPageVO;
import com.auth.service.auth.domain.session.model.vo.OnlineUserSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 在线用户分页与统计查询
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class OnlineUserQueryApplicationService {

	private final OnlineUserQueryPort onlineUserQueryPort;

	private final UserBriefQueryPort userBriefQueryPort;

	/**
	 * 分页查询在线用户
	 * @param query 查询条件
	 * @return 分页结果
	 */
	@Transactional(readOnly = true)
	public PageResponse<OnlineUserPageVO> getOnlineUserPage(OnlineUserPageQuery query) {
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

		OnlineUserPageSlice slice = onlineUserQueryPort.pageOnlineUsers(pageIndex, pageSize, allowedUserIds);
		Set<Long> userIds = slice.users().stream().map(OnlineUserEntry::userId).collect(Collectors.toSet());
		Map<Long, UserOnlineBrief> briefMap = userBriefQueryPort.findByIds(userIds);

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
		OnlineUserSummaryVO summary = new OnlineUserSummaryVO();
		summary.setOnlineUserCount(onlineUserQueryPort.countOnlineUsers());
		return summary;
	}

}
