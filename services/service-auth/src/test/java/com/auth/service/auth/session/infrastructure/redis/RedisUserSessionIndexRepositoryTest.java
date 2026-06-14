package com.auth.service.auth.session.infrastructure.redis;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.redis.SecurityRedisKey;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.session.infrastructure.redis.script.UserSessionRedisScripts;
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

	private static final String GLOBAL_KEY = SecurityRedisKey.GLOBAL_SESSIONS.fixedKey();

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

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
	private SetOperations<String, Object> setOperations;

	@Mock
	private ZSetOperations<String, Object> zSetOperations;

	private RedisUserSessionIndexRepository repository;

	@BeforeEach
	void setUp() {
		when(userSessionRedisScripts.registerScript()).thenReturn(registerScript);
		when(userSessionRedisScripts.terminateScript()).thenReturn(terminateScript);
		when(userSessionRedisScripts.terminateOrphanScript()).thenReturn(terminateOrphanScript);
		when(redisTemplate.opsForHash()).thenReturn(hashOperations);
		when(redisTemplate.opsForSet()).thenReturn(setOperations);
		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
		repository = new RedisUserSessionIndexRepository(redisTemplate, userSessionRedisScripts);
	}

	@Test
	@DisplayName("registerSession：Lua 写入 Hash、用户 Set 与全局 ZSet")
	void registerSession_shouldExecuteRegisterScript() {
		UserSessionIndex index = sampleSessionIndex();
		when(redisTemplate.execute(eq(registerScript), anyList(), any())).thenReturn(1L);

		repository.registerSession(index);

		verify(redisTemplate)
			.execute(eq(registerScript),
					eq(List.of(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI),
							SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID), GLOBAL_KEY)),
					any(Object[].class));
	}

	@Test
	@DisplayName("terminateSession：Lua 删除 Hash、用户 Set 与全局 ZSet")
	void terminateSession_shouldExecuteTerminateScript() {
		when(redisTemplate.execute(eq(terminateScript), anyList(), any())).thenReturn(1L);

		repository.terminateSession(TestConstants.USER_ID, TestConstants.JTI);

		verify(redisTemplate)
			.execute(eq(terminateScript),
					eq(List.of(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI),
							SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID), GLOBAL_KEY)),
					eq(TestConstants.JTI));
	}

	@Test
	@DisplayName("terminateAllSessions：逐个 Lua 终止用户全部会话")
	void terminateAllSessions_shouldTerminateEachSession() {
		when(setOperations.members(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID)))
			.thenReturn(Set.of(TestConstants.JTI, "other-jti"));
		when(redisTemplate.execute(eq(terminateScript), anyList(), any())).thenReturn(1L);

		repository.terminateAllSessions(TestConstants.USER_ID);

		verify(redisTemplate).delete(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID));
		verify(redisTemplate, times(2)).execute(eq(terminateScript), anyList(), anyString());
	}

	@Test
	@DisplayName("terminateSession(jti)：从 Hash 解析 userId 后执行 terminate Lua")
	void terminateSessionByJti_shouldTerminateUsingUserIdFromHash() {
		when(hashOperations.entries(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI)))
			.thenReturn(Map.of("userId", TestConstants.USER_ID, "sessionId", TestConstants.JTI));
		when(redisTemplate.execute(eq(terminateScript), anyList(), any())).thenReturn(1L);

		repository.terminateSession(TestConstants.JTI);

		verify(redisTemplate)
			.execute(eq(terminateScript),
					eq(List.of(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI),
							SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID), GLOBAL_KEY)),
					eq(TestConstants.JTI));
	}

	@Test
	@DisplayName("terminateSession(jti)：Hash 丢失时执行 orphan Lua")
	void terminateSessionByJti_shouldExecuteOrphanScriptWhenHashMissing() {
		when(hashOperations.entries(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI))).thenReturn(Map.of());
		when(redisTemplate.execute(eq(terminateOrphanScript), anyList(), any())).thenReturn(1L);

		repository.terminateSession(TestConstants.JTI);

		verify(redisTemplate).execute(eq(terminateOrphanScript),
				eq(List.of(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI), GLOBAL_KEY)), eq(TestConstants.JTI));
		verify(redisTemplate, never()).execute(eq(terminateScript), anyList(), any());
	}

	@Test
	@DisplayName("listUserSessions：Hash 不存在时清理 Set/ZSet 且不返回")
	void listUserSessions_shouldCleanupStaleIndexAndSkipMissingHash() {
		when(setOperations.members(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID)))
			.thenReturn(Set.of(TestConstants.JTI, "stale-jti"));
		when(hashOperations.entries(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI))).thenReturn(
				Map.of("userId", TestConstants.USER_ID, "sessionId", TestConstants.JTI, "loginAt", 1_700_000_000_000L));
		when(hashOperations.entries(SecurityRedisKey.USER_SESSION.key("stale-jti"))).thenReturn(Map.of());
		when(setOperations.remove(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID), "stale-jti"))
			.thenReturn(1L);
		when(zSetOperations.remove(GLOBAL_KEY, "stale-jti")).thenReturn(1L);

		var sessions = repository.listUserSessions(TestConstants.USER_ID);

		// 仅返回 Hash 仍存在的会话
		assertEquals(1, sessions.size());
		assertEquals(TestConstants.JTI, sessions.get(0).getSessionId());
		verify(setOperations).remove(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID), "stale-jti");
		verify(zSetOperations).remove(GLOBAL_KEY, "stale-jti");
	}

	@Test
	@DisplayName("pageGlobalSessions：Hash 不存在时清理 ZSet 且不纳入结果")
	void pageGlobalSessions_shouldCleanupStaleGlobalIndexAndSkipMissingHash() {
		when(zSetOperations.zCard(GLOBAL_KEY)).thenReturn(2L);
		when(zSetOperations.reverseRange(GLOBAL_KEY, 0L, 63L))
			.thenReturn(new LinkedHashSet<>(List.of(TestConstants.JTI, "stale-jti")));
		when(hashOperations.entries(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI)))
			.thenReturn(Map.of("userId", TestConstants.USER_ID, "sessionId", TestConstants.JTI, "loginAt",
					1_700_000_000_000L, "ipAddress", "10.0.0.1"));
		when(hashOperations.entries(SecurityRedisKey.USER_SESSION.key("stale-jti"))).thenReturn(Map.of());
		when(setOperations.isMember(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID), TestConstants.JTI))
			.thenReturn(true);
		when(zSetOperations.remove(GLOBAL_KEY, "stale-jti")).thenReturn(1L);

		var slice = repository.pageGlobalSessions(1, 10, null, null);

		assertEquals(1L, slice.total());
		assertEquals(1, slice.sessions().size());
		assertEquals(TestConstants.JTI, slice.sessions().get(0).getSessionId());
		verify(zSetOperations).remove(GLOBAL_KEY, "stale-jti");
	}

	@Test
	@DisplayName("pageGlobalSessions：支持用户名与 IP 筛选")
	void pageGlobalSessions_shouldApplyUserAndIpFilters() {
		when(zSetOperations.zCard(GLOBAL_KEY)).thenReturn(1L);
		when(zSetOperations.reverseRange(GLOBAL_KEY, 0L, 63L)).thenReturn(Set.of(TestConstants.JTI, "other-jti"));
		when(hashOperations.entries(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI)))
			.thenReturn(Map.of("userId", TestConstants.USER_ID, "sessionId", TestConstants.JTI, "loginAt",
					1_700_000_000_000L, "ipAddress", "10.0.0.1"));
		when(hashOperations.entries(SecurityRedisKey.USER_SESSION.key("other-jti"))).thenReturn(Map.of("userId", 99L,
				"sessionId", "other-jti", "loginAt", 1_700_000_000_100L, "ipAddress", "172.16.0.2"));
		when(setOperations.isMember(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID), TestConstants.JTI))
			.thenReturn(true);
		when(setOperations.isMember(SecurityRedisKey.USER_SESSIONS.key(99L), "other-jti")).thenReturn(true);

		var slice = repository.pageGlobalSessions(1, 10, Set.of(TestConstants.USER_ID), "10.0");

		assertEquals(1L, slice.total());
		assertEquals(1, slice.sessions().size());
		assertEquals(TestConstants.JTI, slice.sessions().get(0).getSessionId());
	}

	@Test
	@DisplayName("cleanupStaleGlobalSessions：清理 Hash 已丢失的 ZSet 成员")
	void cleanupStaleGlobalSessions_shouldRemoveOrphanZsetMembers() {
		when(zSetOperations.reverseRange(GLOBAL_KEY, 0L, 99L)).thenReturn(Set.of(TestConstants.JTI, "orphan-jti"));
		when(hashOperations.entries(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI)))
			.thenReturn(Map.of("userId", TestConstants.USER_ID));
		when(hashOperations.entries(SecurityRedisKey.USER_SESSION.key("orphan-jti"))).thenReturn(Map.of());
		when(zSetOperations.remove(GLOBAL_KEY, "orphan-jti")).thenReturn(1L);

		int cleaned = repository.cleanupStaleGlobalSessions(100);

		assertEquals(1, cleaned);
		verify(zSetOperations).remove(GLOBAL_KEY, "orphan-jti");
	}

	@Test
	@DisplayName("pageGlobalSessions：全局 ZSet 为空时返回空切片")
	void pageGlobalSessions_shouldReturnEmptyWhenGlobalZsetEmpty() {
		when(zSetOperations.zCard(GLOBAL_KEY)).thenReturn(0L);

		var slice = repository.pageGlobalSessions(1, 10, null, null);

		assertEquals(0L, slice.total());
		assertTrue(slice.sessions().isEmpty());
	}

	@Test
	@DisplayName("registerSession：缺少 sessionId 或 userId 时抛异常")
	void registerSession_shouldRejectMissingIdentifiers() {
		UserSessionIndex index = new UserSessionIndex();
		index.setSessionId(" ");
		index.setUserId(TestConstants.USER_ID);

		assertThrows(IllegalArgumentException.class, () -> repository.registerSession(index));
		verifyNoInteractions(redisTemplate);
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
