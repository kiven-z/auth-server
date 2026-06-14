package com.auth.service.auth.session.infrastructure.redis;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.redis.SecurityRedisKey;
import com.auth.service.auth.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.*;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link RedisUserSessionIndexRepository} 会话生命周期单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RedisUserSessionIndexRepository 会话生命周期")
class RedisUserSessionIndexRepositoryTest {

	private static final String GLOBAL_KEY = SecurityRedisKey.GLOBAL_SESSIONS.fixedKey();

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private HashOperations<String, Object, Object> hashOperations;

	@Mock
	private SetOperations<String, Object> setOperations;

	@Mock
	private ZSetOperations<String, Object> zSetOperations;

	private RedisUserSessionIndexRepository repository;

	@BeforeEach
	void setUp() {
		repository = new RedisUserSessionIndexRepository(redisTemplate);
	}

	@Test
	@DisplayName("registerSession：原子写入 Hash、用户 Set 与全局 ZSet")
	void registerSession_shouldWriteHashSetAndGlobalZset() {
		UserSessionIndex index = sampleSessionIndex();
		RedisOperations<String, Object> operations = mockRedisOperations();
		stubSessionCallback(operations);

		repository.registerSession(index);

		verify(hashOperations).putAll(eq(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI)), anyMap());
		verify(operations).expire(eq(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI)), any(Duration.class));
		verify(setOperations).add(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID), TestConstants.JTI);
		verify(zSetOperations).add(GLOBAL_KEY, TestConstants.JTI, 1_700_000_000_000D);
	}

	@Test
	@DisplayName("terminateSession：原子删除 Hash、用户 Set 与全局 ZSet")
	void terminateSession_shouldRemoveHashSetAndGlobalZset() {
		RedisOperations<String, Object> operations = mockRedisOperations();
		stubSessionCallback(operations);

		repository.terminateSession(TestConstants.USER_ID, TestConstants.JTI);

		verify(operations).delete(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI));
		verify(setOperations).remove(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID), TestConstants.JTI);
		verify(zSetOperations).remove(GLOBAL_KEY, TestConstants.JTI);
	}

	@Test
	@DisplayName("terminateAllSessions：逐个终止用户全部会话")
	void terminateAllSessions_shouldTerminateEachSession() {
		when(redisTemplate.opsForSet()).thenReturn(setOperations);
		when(setOperations.members(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID)))
			.thenReturn(Set.of(TestConstants.JTI, "other-jti"));
		RedisOperations<String, Object> operations = mockRedisOperations();
		stubSessionCallback(operations);

		repository.terminateAllSessions(TestConstants.USER_ID);

		verify(redisTemplate).delete(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID));
		verify(redisTemplate, times(2)).execute(any(SessionCallback.class));
	}

	@Test
	@DisplayName("terminateSession(jti)：从 Hash 解析 userId 后终止")
	void terminateSessionByJti_shouldTerminateUsingUserIdFromHash() {
		when(redisTemplate.opsForHash()).thenReturn(hashOperations);
		when(hashOperations.entries(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI)))
			.thenReturn(Map.of("userId", TestConstants.USER_ID, "sessionId", TestConstants.JTI));
		RedisOperations<String, Object> operations = mockRedisOperations();
		stubSessionCallback(operations);

		repository.terminateSession(TestConstants.JTI);

		verify(zSetOperations).remove(GLOBAL_KEY, TestConstants.JTI);
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
	 * 让 redisTemplate.execute 执行给定 operations 上的 SessionCallback
	 * @param operations Redis 事务操作 mock
	 */
	private void stubSessionCallback(RedisOperations<String, Object> operations) {
		when(redisTemplate.execute(any(SessionCallback.class)))
			.thenAnswer(invocation -> ((SessionCallback<?>) invocation.getArgument(0)).execute(operations));
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

	/**
	 * 构造可执行 MULTI/EXEC 回调的 RedisOperations mock
	 * @return RedisOperations
	 */
	@SuppressWarnings("unchecked")
	private RedisOperations<String, Object> mockRedisOperations() {
		RedisOperations<String, Object> operations = mock(RedisOperations.class);
		doNothing().when(operations).multi();
		when(operations.exec()).thenReturn(Collections.emptyList());
		when(operations.opsForHash()).thenReturn(hashOperations);
		when(operations.opsForSet()).thenReturn(setOperations);
		when(operations.opsForZSet()).thenReturn(zSetOperations);
		when(operations.delete(anyString())).thenReturn(true);
		when(redisTemplate.opsForHash()).thenReturn(hashOperations);
		when(redisTemplate.opsForSet()).thenReturn(setOperations);
		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
		when(operations.expire(anyString(), any(Duration.class))).thenReturn(true);
		return operations;
	}

}
