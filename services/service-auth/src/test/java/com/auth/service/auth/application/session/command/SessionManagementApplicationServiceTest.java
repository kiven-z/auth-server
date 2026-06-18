package com.auth.service.auth.application.session.command;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.application.port.session.SessionPort;
import com.auth.service.auth.application.port.session.SessionRevocationPort;
import com.auth.service.auth.TestConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link SessionManagementApplicationService} 单元测试
 */
@DisplayName("SessionManagementApplicationService 会话管理")
@ExtendWith(MockitoExtension.class)
class SessionManagementApplicationServiceTest {

	@Mock
	private SessionRevocationPort sessionRevocationPort;

	@Mock
	private SessionPort sessionPort;

	@InjectMocks
	private SessionManagementApplicationService sessionManagementApplicationService;

	@Test
	@DisplayName("踢出指定会话：委托 SessionPort.terminateSession")
	void kickSessionShouldDelegateToPort() {
		sessionManagementApplicationService.kickSession(TestConstants.USER_ID, TestConstants.JTI);

		verify(sessionPort).terminateSession(TestConstants.USER_ID, TestConstants.JTI);
		verifyNoInteractions(sessionRevocationPort);
	}

	@Test
	@DisplayName("踢出用户全部会话：委托 SessionRevocationPort")
	void kickAllSessionsByUserIdShouldDelegateToPort() {
		sessionManagementApplicationService.kickAllSessions(TestConstants.USER_ID);

		verify(sessionRevocationPort).kickAllSessions(TestConstants.USER_ID);
	}

	@Test
	@DisplayName("批量踢出全部会话：委托 SessionRevocationPort")
	void kickAllSessionsByUserIdsShouldDelegateToPort() {
		List<Long> userIds = List.of(TestConstants.USER_ID, 2L);

		sessionManagementApplicationService.kickAllSessions(userIds);

		verify(sessionRevocationPort).kickAllSessions(userIds);
	}

	@Test
	@DisplayName("查询活跃会话：委托 SessionPort 并返回结果")
	void listActiveSessionsShouldDelegateToPort() {
		List<UserSessionIndex> sessions = List.of();
		when(sessionPort.listActiveSessions(TestConstants.USER_ID)).thenReturn(sessions);

		List<UserSessionIndex> result = sessionManagementApplicationService.listActiveSessions(TestConstants.USER_ID);

		verify(sessionPort).listActiveSessions(TestConstants.USER_ID);
		assertSame(sessions, result);
	}

}
