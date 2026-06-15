package com.auth.service.auth.application.session;

import com.auth.service.auth.TestConstants;
import com.auth.service.auth.application.port.UserBriefQueryPort;
import com.auth.service.auth.application.port.session.OnlineUserQueryPort;
import com.auth.service.auth.domain.session.model.OnlineUserPageSlice;
import com.auth.service.auth.domain.session.model.OnlineUserPageSlice.OnlineUserEntry;
import com.auth.service.auth.domain.session.model.UserOnlineBrief;
import com.auth.service.auth.domain.session.model.query.OnlineUserPageQuery;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link OnlineUserQueryApplicationService} 单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OnlineUserQueryApplicationServiceTest {

	@Mock
	private OnlineUserQueryPort onlineUserQueryPort;

	@Mock
	private UserBriefQueryPort userBriefQueryPort;

	@InjectMocks
	private OnlineUserQueryApplicationService onlineUserQueryApplicationService;

	@Test
	@DisplayName("在线用户分页：无用户名筛选时 allowedUserIds 为 null")
	void getOnlineUserPage_shouldQueryAllWhenUsernameBlank() {
		OnlineUserPageQuery query = new OnlineUserPageQuery();
		query.setPageIndex(1);
		query.setPageSize(20);

		OnlineUserEntry entry = new OnlineUserEntry(TestConstants.USER_ID, 1_700_000_000_000L, 2);
		when(onlineUserQueryPort.pageOnlineUsers(1, 20, null)).thenReturn(new OnlineUserPageSlice(1L, List.of(entry)));

		UserOnlineBrief userBrief = new UserOnlineBrief(TestConstants.USER_ID, "alice", "Alice");
		when(userBriefQueryPort.findByIds(any())).thenReturn(Map.of(TestConstants.USER_ID, userBrief));

		var page = onlineUserQueryApplicationService.getOnlineUserPage(query);

		verify(onlineUserQueryPort).pageOnlineUsers(1, 20, null);
		verify(userBriefQueryPort, never()).findIdsByUsernameLike(any());
		assertEquals(1L, page.getTotal());
		assertEquals(1, page.getList().size());
		assertEquals("alice", page.getList().get(0).getUsername());
		assertEquals(2, page.getList().get(0).getActiveSessionCount());
	}

	@Test
	@DisplayName("在线用户分页：用户名筛选无匹配时返回空且不访问 Redis 分页")
	void getOnlineUserPage_shouldReturnEmptyWhenUsernameFilterMisses() {
		OnlineUserPageQuery query = new OnlineUserPageQuery();
		query.setUsername("nobody");
		when(userBriefQueryPort.findIdsByUsernameLike("nobody")).thenReturn(List.of());

		var page = onlineUserQueryApplicationService.getOnlineUserPage(query);

		assertEquals(0L, page.getTotal());
		assertTrue(page.getList().isEmpty());
		verify(onlineUserQueryPort, never()).pageOnlineUsers(any(Integer.class), any(Integer.class), any());
	}

	@Test
	@DisplayName("在线用户分页：用户名筛选有匹配时传入 allowedUserIds")
	void getOnlineUserPage_shouldPassAllowedUserIdsWhenUsernameProvided() {
		OnlineUserPageQuery query = new OnlineUserPageQuery();
		query.setUsername("ali");
		when(userBriefQueryPort.findIdsByUsernameLike("ali")).thenReturn(List.of(TestConstants.USER_ID));
		when(onlineUserQueryPort.pageOnlineUsers(eq(1), eq(30), eq(Set.of(TestConstants.USER_ID))))
			.thenReturn(new OnlineUserPageSlice(0L, List.of()));

		onlineUserQueryApplicationService.getOnlineUserPage(query);

		verify(onlineUserQueryPort).pageOnlineUsers(1, 30, Set.of(TestConstants.USER_ID));
	}

	@Test
	@DisplayName("统计摘要：委托 Port 统计在线用户 ZSet 条数")
	void getOnlineUserSummary_shouldReturnOnlineUserCount() {
		when(onlineUserQueryPort.countOnlineUsers()).thenReturn(12L);

		var summary = onlineUserQueryApplicationService.getOnlineUserSummary();

		assertEquals(12L, summary.getOnlineUserCount());
		verify(onlineUserQueryPort).countOnlineUsers();
	}

}
