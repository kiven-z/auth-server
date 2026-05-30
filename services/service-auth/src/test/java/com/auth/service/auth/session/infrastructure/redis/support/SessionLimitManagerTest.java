package com.auth.service.auth.session.infrastructure.redis.support;

import com.auth.module.security.autoconfigure.config.user.SessionLimitStrategy;
import com.auth.module.security.autoconfigure.config.user.UserConfigProperties;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.session.infrastructure.redis.RedisUserSessionIndexRepository;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * SessionLimitManager 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class SessionLimitManagerTest {

	@Mock
	private RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	@Mock
	private UserConfigProperties userConfigProperties;

	@Test
	@DisplayName("会话上限：maxSessionCount 为空或<=0 时直接放行")
	void assertSessionLimit_shouldReturn_whenMaxSessionCountNullOrLe0() {
		// Arrange
		when(userConfigProperties.getMaxSessionCount()).thenReturn(null);
		SessionLimitManager manager = new SessionLimitManager(redisUserSessionIndexRepository, userConfigProperties);

		// Act
		manager.assertSessionLimit(TestConstants.USER_ID);

		// Assert
		verify(redisUserSessionIndexRepository).cleanupStaleActiveSessions(TestConstants.USER_ID);
		verify(redisUserSessionIndexRepository, never()).countActiveSessions(anyLong());

		// <=0
		reset(redisUserSessionIndexRepository);
		when(userConfigProperties.getMaxSessionCount()).thenReturn(0);
		manager.assertSessionLimit(TestConstants.USER_ID);
		verify(redisUserSessionIndexRepository).cleanupStaleActiveSessions(TestConstants.USER_ID);
		verify(redisUserSessionIndexRepository, never()).countActiveSessions(anyLong());
	}

	@Test
	@DisplayName("会话上限：活跃会话数未达上限时放行")
	void assertSessionLimit_shouldReturn_whenCountBelowMax() {
		// Arrange
		when(userConfigProperties.getMaxSessionCount()).thenReturn(3);
		when(redisUserSessionIndexRepository.countActiveSessions(TestConstants.USER_ID)).thenReturn(2L);

		SessionLimitManager manager = new SessionLimitManager(redisUserSessionIndexRepository, userConfigProperties);

		// Act
		manager.assertSessionLimit(TestConstants.USER_ID);

		// Assert
		verify(redisUserSessionIndexRepository).cleanupStaleActiveSessions(TestConstants.USER_ID);
		verify(redisUserSessionIndexRepository).countActiveSessions(TestConstants.USER_ID);
		verify(redisUserSessionIndexRepository, never()).evictOldestActiveSession(anyLong());
	}

	@Test
	@DisplayName("会话上限：策略为空或 REJECT_LOGIN 时抛 MAX_SESSION_LIMIT_EXCEEDED")
	void assertSessionLimit_shouldThrow_whenStrategyNullOrRejectLogin() {
		// Arrange
		when(userConfigProperties.getMaxSessionCount()).thenReturn(1);
		when(redisUserSessionIndexRepository.countActiveSessions(TestConstants.USER_ID)).thenReturn(1L);

		// strategy null
		when(userConfigProperties.getSessionLimitStrategy()).thenReturn(null);
		SessionLimitManager manager = new SessionLimitManager(redisUserSessionIndexRepository, userConfigProperties);
		assertEquals(AuthResultCode.MAX_SESSION_LIMIT_EXCEEDED,
				assertThrows(AuthBusinessException.class, () -> manager.assertSessionLimit(TestConstants.USER_ID))
					.getResultCode());

		// REJECT_LOGIN
		reset(userConfigProperties);
		when(userConfigProperties.getMaxSessionCount()).thenReturn(1);
		when(userConfigProperties.getSessionLimitStrategy()).thenReturn(SessionLimitStrategy.REJECT_LOGIN);
		assertEquals(AuthResultCode.MAX_SESSION_LIMIT_EXCEEDED,
				assertThrows(AuthBusinessException.class, () -> manager.assertSessionLimit(TestConstants.USER_ID))
					.getResultCode());
	}

	@Test
	@DisplayName("会话上限：EVICT_OLDEST 时执行淘汰并重算，仍超限则抛异常")
	void assertSessionLimit_shouldEvictOldest_andRecount_whenEVICT_OLDEST() {
		// Arrange
		when(userConfigProperties.getMaxSessionCount()).thenReturn(1);
		when(userConfigProperties.getSessionLimitStrategy()).thenReturn(SessionLimitStrategy.EVICT_OLDEST);
		when(redisUserSessionIndexRepository.countActiveSessions(TestConstants.USER_ID)).thenReturn(1L, 1L);

		SessionLimitManager manager = new SessionLimitManager(redisUserSessionIndexRepository, userConfigProperties);

		// Act
		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> manager.assertSessionLimit(TestConstants.USER_ID));

		// Assert
		assertEquals(AuthResultCode.MAX_SESSION_LIMIT_EXCEEDED, exception.getResultCode());
		verify(redisUserSessionIndexRepository).evictOldestActiveSession(TestConstants.USER_ID);
	}

}
