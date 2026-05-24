package com.auth.service.auth.session.application;

import com.auth.service.auth.TestConstants;
import com.auth.service.auth.session.infrastructure.redis.RedisUserSessionIndexRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * {@link SessionAdminApplicationService} 批量踢会话单元测试
 */
@ExtendWith(MockitoExtension.class)
class SessionAdminApplicationServiceTest {

	@Mock
	private RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	@InjectMocks
	private SessionAdminApplicationService sessionAdminApplicationService;

	@Test
	@DisplayName("批量踢会话：入参为空/含 null 时不抛异常且跳过 null；下游异常会被吞掉")
	void kickAllSessionsBatch_shouldIgnoreEmptyAndNullAndSwallowExceptions() {
		assertDoesNotThrow(() -> sessionAdminApplicationService.kickAllSessions(List.of()));
		verifyNoInteractions(redisUserSessionIndexRepository);

		doThrow(new RuntimeException("boom")).when(redisUserSessionIndexRepository)
			.deleteAllSessions(TestConstants.USER_ID);
		assertDoesNotThrow(
				() -> sessionAdminApplicationService.kickAllSessions(Arrays.asList(null, TestConstants.USER_ID)));
		verify(redisUserSessionIndexRepository).deleteAllSessions(TestConstants.USER_ID);
	}

}
