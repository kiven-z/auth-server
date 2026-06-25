package com.auth.service.auth.application.session.query;

import com.auth.common.data.model.PageResponse;
import com.auth.service.auth.api.dto.session.request.OnlineUserPageQuery;
import com.auth.service.auth.api.dto.session.response.OnlineUserPageVO;
import com.auth.service.auth.application.port.UserReadPort;
import com.auth.service.auth.application.port.session.OnlineUserQueryPort;
import com.auth.service.auth.domain.session.model.OnlineUserPageSlice;
import com.auth.service.auth.domain.session.model.OnlineUserPageSlice.OnlineUserEntry;
import com.auth.service.auth.domain.session.model.UserOnlineBrief;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 在线用户分页查询
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class OnlineUserQueryService {

	private final OnlineUserQueryPort onlineUserQueryPort;

	private final UserReadPort userReadPort;

	/**
	 * 分页查询在线用户
	 * @param query HTTP 查询条件
	 * @return 分页 VO
	 */
	public PageResponse<OnlineUserPageVO> getOnlineUserPage(OnlineUserPageQuery query) {
		int pageIndex = Objects.requireNonNullElse(query.getPageIndex(), 1);
		int pageSize = Objects.requireNonNullElse(query.getPageSize(), 30);

		Long userId = query.getUserId();
		Set<Long> allowedUserIds = userId != null ? Set.of(userId) : null;

		OnlineUserPageSlice slice = onlineUserQueryPort.pageOnlineUsers(pageIndex, pageSize, allowedUserIds);
		Set<Long> userIds = slice.users().stream().map(OnlineUserEntry::userId).collect(Collectors.toSet());

		Map<Long, UserOnlineBrief> briefMap = userReadPort.findOnlineBriefByIds(userIds);
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

}
