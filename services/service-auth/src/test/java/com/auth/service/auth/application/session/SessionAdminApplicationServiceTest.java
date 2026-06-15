package com.auth.service.auth.application.session;

import com.auth.service.auth.TestConstants;
import com.auth.service.auth.application.port.UserBriefQueryPort;
import com.auth.service.auth.domain.session.model.OnlineUserPageSlice;
import com.auth.service.auth.domain.session.model.OnlineUserPageSlice.OnlineUserEntry;
import com.auth.service.auth.domain.session.model.UserOnlineBrief;
import com.auth.service.auth.domain.session.model.query.OnlineUserPageQuery;
import com.auth.service.auth.infrastructure.redis.session.RedisUserSessionIndexRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link SessionAdminApplicationService} 管理端会话单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SessionAdminApplicationServiceTest {

	@Mock
	private RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	@Mock
	private UserBriefQueryPort userBriefQueryPort;

	@InjectMocks
	private SessionAdminApplicationService sessionAdminApplicationService;

	@Test
	@DisplayName("踢单会话：委托 terminateSession")
	void kickSession_shouldTerminateSingleSession() {
		sessionAdminApplicationService.kickSession(TestConstants.USER_ID, TestConstants.JTI);

		verify(redisUserSessionIndexRepository).terminateSession(TestConstants.USER_ID, TestConstants.JTI);
	}

	@Test
	@DisplayName("批量踢会话：入参为空/含 null 时不抛异常且跳过 null；下游异常会被吞掉")
	void kickAllSessionsBatch_shouldIgnoreEmptyAndNullAndSwallowExceptions() {
		assertDoesNotThrow(() -> sessionAdminApplicationService.kickAllSessions(List.of()));
		verifyNoInteractions(redisUserSessionIndexRepository);

		doThrow(new RuntimeException("boom")).when(redisUserSessionIndexRepository)
			.terminateAllSessions(TestConstants.USER_ID);
		assertDoesNotThrow(
				() -> sessionAdminApplicationService.kickAllSessions(Arrays.asList(null, TestConstants.USER_ID)));
		verify(redisUserSessionIndexRepository).terminateAllSessions(TestConstants.USER_ID);
	}

	@Test
	@DisplayName("在线用户分页：无用户名筛选时 allowedUserIds 为 null")
	void getOnlineUserPage_shouldQueryAllWhenUsernameBlank() {
		OnlineUserPageQuery query = new OnlineUserPageQuery();
		query.setPageIndex(1);
		query.setPageSize(20);

		OnlineUserEntry entry = new OnlineUserEntry(TestConstants.USER_ID, 1_700_000_000_000L, 2);
		when(redisUserSessionIndexRepository.pageOnlineUsers(1, 20, null))
			.thenReturn(new OnlineUserPageSlice(1L, List.of(entry)));

		UserOnlineBrief userBrief = new UserOnlineBrief(TestConstants.USER_ID, "alice", "Alice");
		when(userBriefQueryPort.findByIds(any())).thenReturn(Map.of(TestConstants.USER_ID, userBrief));

		var page = sessionAdminApplicationService.getOnlineUserPage(query);

		verify(redisUserSessionIndexRepository).pageOnlineUsers(1, 20, null);
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

		var page = sessionAdminApplicationService.getOnlineUserPage(query);

		assertEquals(0L, page.getTotal());
		assertTrue(page.getList().isEmpty());
		verify(redisUserSessionIndexRepository, never()).pageOnlineUsers(any(Integer.class), any(Integer.class), any());
	}

	@Test
	@DisplayName("在线用户分页：用户名筛选有匹配时传入 allowedUserIds")
	void getOnlineUserPage_shouldPassAllowedUserIdsWhenUsernameProvided() {
		OnlineUserPageQuery query = new OnlineUserPageQuery();
		query.setUsername("ali");
		when(userBriefQueryPort.findIdsByUsernameLike("ali")).thenReturn(List.of(TestConstants.USER_ID));
		when(redisUserSessionIndexRepository.pageOnlineUsers(eq(1), eq(30), eq(Set.of(TestConstants.USER_ID))))
			.thenReturn(new OnlineUserPageSlice(0L, List.of()));

		sessionAdminApplicationService.getOnlineUserPage(query);

		verify(redisUserSessionIndexRepository).pageOnlineUsers(1, 30, Set.of(TestConstants.USER_ID));
	}

	@Test
	@DisplayName("统计摘要：委托 Repository 统计在线用户 ZSet 条数")
	void getOnlineUserSummary_shouldReturnOnlineUserCount() {
		when(redisUserSessionIndexRepository.countOnlineUsers()).thenReturn(12L);

		var summary = sessionAdminApplicationService.getOnlineUserSummary();

		assertEquals(12L, summary.getOnlineUserCount());
		verify(redisUserSessionIndexRepository).countOnlineUsers();
	}

}
