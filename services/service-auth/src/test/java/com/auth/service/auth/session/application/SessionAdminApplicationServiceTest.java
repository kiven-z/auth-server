package com.auth.service.auth.session.application;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.authentication.infrastructure.persistence.UserMapper;
import com.auth.service.auth.session.domain.model.OnlineSessionPageSlice;
import com.auth.service.auth.session.domain.model.query.OnlineSessionPageQuery;
import com.auth.service.auth.session.infrastructure.redis.RedisUserSessionIndexRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link SessionAdminApplicationService} 管理端会话单元测试
 */
@ExtendWith(MockitoExtension.class)
class SessionAdminApplicationServiceTest {

	@Mock
	private RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	@Mock
	private UserMapper userMapper;

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
	@DisplayName("全局在线分页：无用户名筛选时 allowedUserIds 为 null")
	void getOnlineSessionPage_shouldQueryAllWhenUsernameBlank() {
		OnlineSessionPageQuery query = new OnlineSessionPageQuery();
		query.setPageIndex(1);
		query.setPageSize(20);

		UserSessionIndex session = sampleSession();
		when(redisUserSessionIndexRepository.cleanupStaleGlobalSessions(100)).thenReturn(0);
		when(redisUserSessionIndexRepository.pageGlobalSessions(1, 20, null, null))
			.thenReturn(new OnlineSessionPageSlice(1L, List.of(session)));

		UserEntity userEntity = new UserEntity();
		userEntity.setId(TestConstants.USER_ID);
		userEntity.setUsername("alice");
		userEntity.setNickname("Alice");
		when(userMapper.selectList(any())).thenReturn(List.of(userEntity));

		var page = sessionAdminApplicationService.getOnlineSessionPage(query);

		verify(redisUserSessionIndexRepository).cleanupStaleGlobalSessions(100);
		verify(redisUserSessionIndexRepository).pageGlobalSessions(1, 20, null, null);
		verify(userMapper, never()).selectIdsByUsernameLike(any());
		assertEquals(1L, page.getTotal());
		assertEquals(1, page.getList().size());
		assertEquals("alice", page.getList().get(0).getUsername());
		assertEquals(TestConstants.JTI, page.getList().get(0).getSessionId());
	}

	@Test
	@DisplayName("全局在线分页：用户名筛选无匹配时返回空且不访问 Redis 分页")
	void getOnlineSessionPage_shouldReturnEmptyWhenUsernameFilterMisses() {
		OnlineSessionPageQuery query = new OnlineSessionPageQuery();
		query.setUsername("nobody");
		when(redisUserSessionIndexRepository.cleanupStaleGlobalSessions(100)).thenReturn(0);
		when(userMapper.selectIdsByUsernameLike("nobody")).thenReturn(List.of());

		var page = sessionAdminApplicationService.getOnlineSessionPage(query);

		assertEquals(0L, page.getTotal());
		assertTrue(page.getList().isEmpty());
		verify(redisUserSessionIndexRepository).cleanupStaleGlobalSessions(100);
		verify(redisUserSessionIndexRepository, never()).pageGlobalSessions(any(Integer.class), any(Integer.class),
				any(), any());
	}

	@Test
	@DisplayName("全局在线分页：用户名筛选有匹配时传入 allowedUserIds")
	void getOnlineSessionPage_shouldPassAllowedUserIdsWhenUsernameProvided() {
		OnlineSessionPageQuery query = new OnlineSessionPageQuery();
		query.setUsername("ali");
		query.setIpAddress("192.168");
		when(redisUserSessionIndexRepository.cleanupStaleGlobalSessions(100)).thenReturn(0);
		when(userMapper.selectIdsByUsernameLike("ali")).thenReturn(List.of(TestConstants.USER_ID));
		when(redisUserSessionIndexRepository.pageGlobalSessions(eq(1), eq(30), eq(Set.of(TestConstants.USER_ID)),
				eq("192.168")))
			.thenReturn(new OnlineSessionPageSlice(0L, List.of()));

		sessionAdminApplicationService.getOnlineSessionPage(query);

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
