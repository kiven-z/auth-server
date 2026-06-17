package com.auth.service.auth.application.session.query;

import com.auth.service.auth.TestConstants;
import com.auth.service.auth.api.dto.session.request.OnlineUserPageQuery;
import com.auth.service.auth.application.port.UserReadPort;
import com.auth.service.auth.application.port.session.OnlineUserQueryPort;
import com.auth.service.auth.domain.session.model.OnlineUserPageSlice;
import com.auth.service.auth.domain.session.model.OnlineUserPageSlice.OnlineUserEntry;
import com.auth.service.auth.domain.session.model.UserOnlineBrief;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link OnlineUserQueryService} 单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OnlineUserQueryServiceTest {

	@Mock
	private OnlineUserQueryPort onlineUserQueryPort;

	@Mock
	private UserReadPort userReadPort;

	@InjectMocks
	private OnlineUserQueryService onlineUserQueryService;

	@Test
	@DisplayName("在线用户分页：无用户筛选时 allowedUserIds 为 null")
	void getOnlineUserPage_shouldQueryAllWhenUserIdBlank() {
		OnlineUserPageQuery query = new OnlineUserPageQuery();
		query.setPageIndex(1);
		query.setPageSize(20);

		OnlineUserEntry entry = new OnlineUserEntry(TestConstants.USER_ID, 1_700_000_000_000L, 2);
		when(onlineUserQueryPort.pageOnlineUsers(1, 20, null)).thenReturn(new OnlineUserPageSlice(1L, List.of(entry)));

		UserOnlineBrief userBrief = new UserOnlineBrief(TestConstants.USER_ID, "alice", "Alice");
		when(userReadPort.findOnlineBriefByIds(any())).thenReturn(Map.of(TestConstants.USER_ID, userBrief));

		var page = onlineUserQueryService.getOnlineUserPage(query);

		verify(onlineUserQueryPort).pageOnlineUsers(1, 20, null);
		assertEquals(1L, page.getTotal());
		assertEquals(1, page.getList().size());
		assertEquals("alice", page.getList().get(0).getUsername());
		assertEquals(2, page.getList().get(0).getActiveSessionCount());
	}

	@Test
	@DisplayName("在线用户分页：传入 userId 时按单用户筛选")
	void getOnlineUserPage_shouldPassAllowedUserIdsWhenUserIdProvided() {
		OnlineUserPageQuery query = new OnlineUserPageQuery();
		query.setUserId(TestConstants.USER_ID);
		when(onlineUserQueryPort.pageOnlineUsers(1, 30, Set.of(TestConstants.USER_ID)))
			.thenReturn(new OnlineUserPageSlice(0L, List.of()));

		onlineUserQueryService.getOnlineUserPage(query);

		verify(onlineUserQueryPort).pageOnlineUsers(1, 30, Set.of(TestConstants.USER_ID));
	}

	@Test
	@DisplayName("统计摘要：委托 Port 统计在线用户 ZSet 条数")
	void getOnlineUserCount_shouldReturnOnlineUserCount() {
		when(onlineUserQueryPort.countOnlineUsers()).thenReturn(12L);

		long count = onlineUserQueryService.getOnlineUserCount();

		assertEquals(12L, count);
		verify(onlineUserQueryPort).countOnlineUsers();
	}

}
