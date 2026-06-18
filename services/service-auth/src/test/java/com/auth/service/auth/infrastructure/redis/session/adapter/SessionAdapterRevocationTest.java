package com.auth.service.auth.infrastructure.redis.session.adapter;

import com.auth.service.auth.TestConstants;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

/**
 * {@link SessionAdapter} 会话撤销单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SessionAdapterRevocationTest {

	@Mock
	private RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	@InjectMocks
	private SessionAdapter sessionAdapter;

	@Test
	@DisplayName("终止单会话：委托 Redis 仓储")
	void terminateSession_shouldDelegateToRepository() {
		sessionAdapter.terminateSession(TestConstants.USER_ID, TestConstants.JTI);

		verify(redisUserSessionIndexRepository).terminateSession(TestConstants.USER_ID, TestConstants.JTI);
	}

	@Test
	@DisplayName("批量踢会话：入参为空/含 null 时不抛异常且跳过 null；下游异常会被吞掉")
	void kickAllSessionsBatch_shouldIgnoreEmptyAndNullAndSwallowExceptions() {
		assertDoesNotThrow(() -> sessionAdapter.kickAllSessions(List.of()));
		verifyNoInteractions(redisUserSessionIndexRepository);

		doThrow(new RuntimeException("boom")).when(redisUserSessionIndexRepository)
			.terminateAllSessions(TestConstants.USER_ID);
		assertDoesNotThrow(() -> sessionAdapter.kickAllSessions(Arrays.asList(null, TestConstants.USER_ID)));
		verify(redisUserSessionIndexRepository).terminateAllSessions(TestConstants.USER_ID);
	}

}
