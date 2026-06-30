package com.auth.service.system.authorization.dispatch.trigger;

import java.util.Collections;
import java.util.List;

import com.auth.service.system.authorization.service.UserSessionRevocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * {@link UserSessionRevocationTrigger} 单元测试
 */
@DisplayName("UserSessionRevocationTrigger 会话撤销")
@ExtendWith(MockitoExtension.class)
class UserSessionRevocationTriggerTest {

	@Mock
	private UserSessionRevocationService userSessionRevocationService;

	@InjectMocks
	private UserSessionRevocationTrigger userSessionRevocationTrigger;

	@Test
	@DisplayName("空列表时跳过撤销")
	void revokeAllSessionsAfterCommit_whenEmpty_shouldSkip() {
		userSessionRevocationTrigger.revokeAllSessionsAfterCommit(Collections.emptyList());

		verifyNoInteractions(userSessionRevocationService);
	}

	@Test
	@DisplayName("无活跃事务时立即撤销")
	void revokeAllSessionsAfterCommit_whenNoTransaction_shouldRevokeImmediately() {
		userSessionRevocationTrigger.revokeAllSessionsAfterCommit(List.of(2L, 3L));

		verify(userSessionRevocationService).revokeAllSessions(List.of(2L, 3L));
	}

}
