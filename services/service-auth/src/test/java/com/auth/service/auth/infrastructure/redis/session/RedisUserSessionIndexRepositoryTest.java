package com.auth.service.auth.infrastructure.redis.session;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.redis.SecurityRedisKey;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.infrastructure.redis.session.script.UserSessionRedisScripts;
import com.auth.service.auth.infrastructure.redis.session.support.UserSessionIndexRedisCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link RedisUserSessionIndexRepository} 会话生命周期单元测试
 */
@DisplayName("RedisUserSessionIndexRepository 会话生命周期")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RedisUserSessionIndexRepositoryTest {

	private static final String ONLINE_USERS_KEY = SecurityRedisKey.ONLINE_USERS.fixedKey();

	@Mock
	private RedisTemplate<String, String> sessionRedisTemplate;

	@Mock
	private UserSessionRedisScripts userSessionRedisScripts;

	@Mock
	private DefaultRedisScript<Long> registerScript;

	@Mock
	private DefaultRedisScript<Long> terminateScript;

	@Mock
	private DefaultRedisScript<Long> terminateOrphanScript;

	@Mock
	private HashOperations<String, Object, Object> hashOperations;

	@Mock
	private SetOperations<String, String> setOperations;

	@Mock
	private ZSetOperations<String, String> zSetOperations;

	private RedisUserSessionIndexRepository repository;

	@BeforeEach
	void setUp() {
		when(userSessionRedisScripts.registerScript()).thenReturn(registerScript);
		when(userSessionRedisScripts.terminateScript()).thenReturn(terminateScript);
		when(userSessionRedisScripts.terminateOrphanScript()).thenReturn(terminateOrphanScript);
		when(sessionRedisTemplate.opsForHash()).thenReturn(hashOperations);
		when(sessionRedisTemplate.opsForSet()).thenReturn(setOperations);
		when(sessionRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
		UserSessionIndexRedisCodec codec = new UserSessionIndexRedisCodec();
		UserSessionLifecycleStore lifecycleStore = new UserSessionLifecycleStore(sessionRedisTemplate,
				userSessionRedisScripts, codec);
		UserSessionIndexStore indexStore = new UserSessionIndexStore(sessionRedisTemplate, codec, lifecycleStore);
		repository = new RedisUserSessionIndexRepository(lifecycleStore, indexStore, sessionRedisTemplate);
	}

	@Test
	@DisplayName("registerSession：Lua 写入 Hash、用户 Set 与在线用户 ZSet")
	void registerSession_shouldExecuteRegisterScript() {
		UserSessionIndex index = sampleSessionIndex();
		when(sessionRedisTemplate.execute(eq(registerScript), anyList(), any())).thenReturn(1L);

		repository.registerSession(index);

		verify(sessionRedisTemplate).execute(eq(registerScript),
				eq(List.of(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI),
						SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID), ONLINE_USERS_KEY)),
				any(Object[].class));
	}

	@Test
	@DisplayName("terminateSession：Lua 删除 Hash 与用户 Set，并在无会话时移出在线用户 ZSet")
	void terminateSession_shouldExecuteTerminateScriptAndSyncOnlineUser() {
		when(sessionRedisTemplate.execute(eq(terminateScript), anyList(), any())).thenReturn(1L);
		when(setOperations.size(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID))).thenReturn(0L);

		repository.terminateSession(TestConstants.USER_ID, TestConstants.JTI);

		verify(sessionRedisTemplate).execute(terminateScript,
				List.of(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI),
						SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID)),
				TestConstants.JTI);
		verify(zSetOperations).remove(ONLINE_USERS_KEY, String.valueOf(TestConstants.USER_ID));
	}

	@Test
	@DisplayName("terminateAllSessions：逐个 Lua 终止用户全部会话并移出在线用户 ZSet")
	void terminateAllSessions_shouldTerminateEachSessionAndRemoveOnlineUser() {
		when(setOperations.members(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID)))
			.thenReturn(Set.of(TestConstants.JTI, "other-jti"));
		when(sessionRedisTemplate.execute(eq(terminateScript), anyList(), any())).thenReturn(1L);
		when(setOperations.size(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID))).thenReturn(0L);

		repository.terminateAllSessions(TestConstants.USER_ID);

		verify(sessionRedisTemplate).delete(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID));
		verify(sessionRedisTemplate, times(2)).execute(eq(terminateScript), anyList(), anyString());
		verify(zSetOperations).remove(ONLINE_USERS_KEY, String.valueOf(TestConstants.USER_ID));
	}

	@Test
	@DisplayName("terminateSession(jti)：从 Hash 解析 userId 后执行 terminate Lua")
	void terminateSessionByJti_shouldTerminateUsingUserIdFromHash() {
		when(hashOperations.entries(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI)))
			.thenReturn(Map.of("userId", String.valueOf(TestConstants.USER_ID), "sessionId", TestConstants.JTI));
		when(sessionRedisTemplate.execute(eq(terminateScript), anyList(), any())).thenReturn(1L);
		when(setOperations.size(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID))).thenReturn(0L);

		repository.terminateSession(TestConstants.JTI);

		verify(sessionRedisTemplate).execute(terminateScript,
				List.of(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI),
						SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID)),
				TestConstants.JTI);
	}

	@Test
	@DisplayName("terminateSession(jti)：Hash 丢失时执行 orphan Lua")
	void terminateSessionByJti_shouldExecuteOrphanScriptWhenHashMissing() {
		when(hashOperations.entries(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI))).thenReturn(Map.of());
		when(sessionRedisTemplate.execute(eq(terminateOrphanScript), anyList(), any())).thenReturn(1L);

		repository.terminateSession(TestConstants.JTI);

		verify(sessionRedisTemplate).execute(terminateOrphanScript,
				List.of(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI)), TestConstants.JTI);
		verify(sessionRedisTemplate, never()).execute(eq(terminateScript), anyList(), any());
	}

	@Test
	@DisplayName("listUserSessions：Hash 不存在时清理 Set 且不返回")
	void listUserSessions_shouldCleanupStaleIndexAndSkipMissingHash() {
		when(setOperations.members(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID)))
			.thenReturn(Set.of(TestConstants.JTI, "stale-jti"));
		when(hashOperations.entries(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI))).thenReturn(Map.of("userId",
				String.valueOf(TestConstants.USER_ID), "sessionId", TestConstants.JTI, "loginAt", "1700000000000"));
		when(hashOperations.entries(SecurityRedisKey.USER_SESSION.key("stale-jti"))).thenReturn(Map.of());
		when(setOperations.remove(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID), "stale-jti"))
			.thenReturn(1L);
		when(setOperations.size(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID))).thenReturn(1L);

		var sessions = repository.listUserSessions(TestConstants.USER_ID);

		assertEquals(1, sessions.size());
		assertEquals(TestConstants.JTI, sessions.get(0).getSessionId());
		verify(setOperations).remove(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID), "stale-jti");
	}

	@Test
	@DisplayName("pageOnlineUsers：无活跃会话时清理 ZSet 且不纳入结果")
	void pageOnlineUsers_shouldCleanupStaleOnlineUserAndSkip() {
		when(zSetOperations.zCard(ONLINE_USERS_KEY)).thenReturn(2L);
		when(zSetOperations.reverseRange(ONLINE_USERS_KEY, 0L, 63L))
			.thenReturn(new LinkedHashSet<>(List.of(String.valueOf(TestConstants.USER_ID), "999")));
		when(setOperations.size(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID))).thenReturn(1L);
		when(zSetOperations.score(ONLINE_USERS_KEY, String.valueOf(TestConstants.USER_ID)))
			.thenReturn(1_700_000_000_000D);
		when(setOperations.size(SecurityRedisKey.USER_SESSIONS.key(999L))).thenReturn(0L);

		var slice = repository.pageOnlineUsers(1, 10, null);

		assertEquals(1L, slice.total());
		assertEquals(1, slice.users().size());
		assertEquals(TestConstants.USER_ID, slice.users().get(0).userId());
		verify(zSetOperations).remove(ONLINE_USERS_KEY, "999");
	}

	@Test
	@DisplayName("pageOnlineUsers：支持用户名筛选")
	void pageOnlineUsers_shouldApplyUserFilter() {
		when(zSetOperations.zCard(ONLINE_USERS_KEY)).thenReturn(1L);
		when(zSetOperations.reverseRange(ONLINE_USERS_KEY, 0L, 63L))
			.thenReturn(Set.of(String.valueOf(TestConstants.USER_ID), "99"));
		when(setOperations.size(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID))).thenReturn(2L);
		when(zSetOperations.score(ONLINE_USERS_KEY, String.valueOf(TestConstants.USER_ID)))
			.thenReturn(1_700_000_000_000D);
		when(setOperations.size(SecurityRedisKey.USER_SESSIONS.key(99L))).thenReturn(1L);
		when(zSetOperations.score(ONLINE_USERS_KEY, "99")).thenReturn(1_700_000_000_100D);

		var slice = repository.pageOnlineUsers(1, 10, Set.of(TestConstants.USER_ID));

		assertEquals(1L, slice.total());
		assertEquals(1, slice.users().size());
		assertEquals(TestConstants.USER_ID, slice.users().get(0).userId());
		assertEquals(2, slice.users().get(0).activeSessionCount());
	}

	@Test
	@DisplayName("pageOnlineUsers：在线用户 ZSet 为空时返回空切片")
	void pageOnlineUsers_shouldReturnEmptyWhenOnlineZsetEmpty() {
		when(zSetOperations.zCard(ONLINE_USERS_KEY)).thenReturn(0L);

		var slice = repository.pageOnlineUsers(1, 10, null);

		assertEquals(0L, slice.total());
		assertTrue(slice.users().isEmpty());
	}

	@Test
	@DisplayName("countOnlineUsers：返回 online users ZSet 基数")
	void countOnlineUsers_shouldReturnZcard() {
		when(zSetOperations.zCard(ONLINE_USERS_KEY)).thenReturn(8L);

		assertEquals(8L, repository.countOnlineUsers());
	}

	@Test
	@DisplayName("syncOnlineUserAfterTerminate：仍有活跃会话时不移出 ZSet")
	void syncOnlineUserAfterTerminate_shouldKeepUserWhenSessionsRemain() {
		when(setOperations.size(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID))).thenReturn(2L);

		repository.syncOnlineUserAfterTerminate(TestConstants.USER_ID);

		verify(zSetOperations, never()).remove(anyString(), anyString());
	}

	@Test
	@DisplayName("registerSession：缺少 sessionId 或 userId 时抛异常")
	void registerSession_shouldRejectMissingIdentifiers() {
		UserSessionIndex index = new UserSessionIndex();
		index.setSessionId(" ");
		index.setUserId(TestConstants.USER_ID);

		assertThrows(IllegalArgumentException.class, () -> repository.registerSession(index));
		verifyNoInteractions(sessionRedisTemplate);
	}

	/**
	 * 构造带 loginAt 的样例会话索引
	 * @return 会话索引
	 */
	private UserSessionIndex sampleSessionIndex() {
		UserSessionIndex index = new UserSessionIndex();
		index.setUserId(TestConstants.USER_ID);
		index.setSessionId(TestConstants.JTI);
		index.setLoginAt(1_700_000_000_000L);
		index.setRefreshTokenExpiresAt(System.currentTimeMillis() + Duration.ofHours(2).toMillis());
		return index;
	}

}
