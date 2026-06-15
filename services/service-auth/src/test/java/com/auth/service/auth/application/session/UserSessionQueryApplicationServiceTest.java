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
	@DisplayName("查询活跃会话：映射为 UserSessionVO 且不含 refreshTokenHash")
	void listActiveSessions_shouldMapToVo() {
		UserSessionIndex index = new UserSessionIndex();
		index.setUserId(TestConstants.USER_ID);
		index.setSessionId(TestConstants.JTI);
		index.setIpAddress("127.0.0.1");
		index.setRefreshTokenHash("secret-hash");
		when(userSessionQueryPort.listActiveSessions(TestConstants.USER_ID)).thenReturn(List.of(index));

		var sessions = userSessionQueryApplicationService.listActiveSessions(TestConstants.USER_ID);

		assertEquals(1, sessions.size());
		assertEquals(TestConstants.USER_ID, sessions.get(0).getUserId());
		assertEquals(TestConstants.JTI, sessions.get(0).getSessionId());
		assertEquals("127.0.0.1", sessions.get(0).getIpAddress());
	}

}
