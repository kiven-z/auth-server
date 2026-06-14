package com.auth.service.auth.session.application;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.session.domain.model.OnlineSessionPageSlice;
import com.auth.service.auth.session.domain.model.UserOnlineBrief;
import com.auth.service.auth.session.domain.model.query.OnlineSessionPageQuery;
import com.auth.service.auth.session.domain.port.OnlineSessionUserLookupPort;
import com.auth.service.auth.session.infrastructure.redis.RedisUserSessionIndexRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link OnlineSessionQueryApplicationService} 全局在线会话查询单元测试
 */
@DisplayName("OnlineSessionQueryApplicationService 全局在线会话")
@ExtendWith(MockitoExtension.class)
class OnlineSessionQueryApplicationServiceTest {

	@Mock
	private RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	@Mock
	private OnlineSessionUserLookupPort onlineSessionUserLookupPort;

	@InjectMocks
	private OnlineSessionQueryApplicationService onlineSessionQueryApplicationService;

	@Test
	@DisplayName("分页查询：查询前预清理僵尸索引并组装用户展示字段")
	void getPage_shouldCleanupStaleAndAssembleUserBrief() {
		OnlineSessionPageQuery query = new OnlineSessionPageQuery();
		query.setPageIndex(1);
		query.setPageSize(20);

		UserSessionIndex session = sampleSession();
		when(redisUserSessionIndexRepository.cleanupStaleGlobalSessions(100)).thenReturn(0);
		when(redisUserSessionIndexRepository.pageGlobalSessions(1, 20, null, null))
			.thenReturn(new OnlineSessionPageSlice(1L, List.of(session)));
		when(onlineSessionUserLookupPort.findBriefByIds(Set.of(TestConstants.USER_ID)))
			.thenReturn(Map.of(TestConstants.USER_ID, new UserOnlineBrief(TestConstants.USER_ID, "alice", "Alice")));

		var page = onlineSessionQueryApplicationService.getPage(query);

		verify(redisUserSessionIndexRepository).cleanupStaleGlobalSessions(100);
		assertEquals(1L, page.getTotal());
		assertEquals(1, page.getList().size());
		assertEquals("alice", page.getList().get(0).getUsername());
		assertEquals(TestConstants.JTI, page.getList().get(0).getSessionId());
	}

	@Test
	@DisplayName("用户名筛选无匹配：返回空分页且不访问 Redis 分页")
	void getPage_shouldReturnEmptyWhenUsernameFilterMisses() {
		OnlineSessionPageQuery query = new OnlineSessionPageQuery();
		query.setUsername("nobody");
		when(redisUserSessionIndexRepository.cleanupStaleGlobalSessions(100)).thenReturn(0);
		when(onlineSessionUserLookupPort.findUserIdsByUsernameLike("nobody")).thenReturn(List.of());

		var page = onlineSessionQueryApplicationService.getPage(query);

		assertEquals(0L, page.getTotal());
		assertTrue(page.getList().isEmpty());
		verify(redisUserSessionIndexRepository).cleanupStaleGlobalSessions(100);
		verify(redisUserSessionIndexRepository, org.mockito.Mockito.never()).pageGlobalSessions(anyInt(), anyInt(),
				any(), any());
	}

	@Test
	@DisplayName("用户名筛选有匹配：将用户 ID 集合传入 Repository")
	void getPage_shouldPassAllowedUserIdsWhenUsernameProvided() {
		OnlineSessionPageQuery query = new OnlineSessionPageQuery();
		query.setUsername("ali");
		query.setIpAddress("192.168");
		when(redisUserSessionIndexRepository.cleanupStaleGlobalSessions(100)).thenReturn(0);
		when(onlineSessionUserLookupPort.findUserIdsByUsernameLike("ali")).thenReturn(List.of(TestConstants.USER_ID));
		when(redisUserSessionIndexRepository.pageGlobalSessions(eq(1), eq(30), eq(Set.of(TestConstants.USER_ID)),
				eq("192.168")))
			.thenReturn(new OnlineSessionPageSlice(0L, List.of()));

		onlineSessionQueryApplicationService.getPage(query);

		verify(redisUserSessionIndexRepository).pageGlobalSessions(1, 30, Set.of(TestConstants.USER_ID), "192.168");
	}

	/**
	 * 构造样例会话索引
	 * @return 会话索引
	 */
	private UserSessionIndex sampleSession() {
		UserSessionIndex session = new UserSessionIndex();
		session.setUserId(TestConstants.USER_ID);
		session.setSessionId(TestConstants.JTI);
		session.setLoginAt(1_700_000_000_000L);
		session.setIpAddress("192.168.1.1");
		return session;
	}

}
