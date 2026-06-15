package com.auth.service.auth.application.session;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.application.port.session.UserSessionQueryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * {@link UserSessionQueryApplicationService} 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserSessionQueryApplicationServiceTest {

	@Mock
	private UserSessionQueryPort userSessionQueryPort;

	@InjectMocks
	private UserSessionQueryApplicationService userSessionQueryApplicationService;

	@Test
	@DisplayName("查询活跃会话：委托 UserSessionQueryPort")
	void listActiveSessions_shouldDelegateToPort() {
		UserSessionIndex index = new UserSessionIndex();
		index.setUserId(TestConstants.USER_ID);
		index.setSessionId(TestConstants.JTI);
		when(userSessionQueryPort.listActiveSessions(TestConstants.USER_ID)).thenReturn(List.of(index));

		List<UserSessionIndex> sessions = userSessionQueryApplicationService.listActiveSessions(TestConstants.USER_ID);

		assertEquals(1, sessions.size());
		assertEquals(TestConstants.JTI, sessions.get(0).getSessionId());
	}

}
