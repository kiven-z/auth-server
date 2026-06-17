package com.auth.service.auth.api.controller;

import com.auth.service.auth.application.session.command.SessionManagementApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

/**
 * {@link SessionInnerController} 单元测试
 */
@DisplayName("SessionInnerController 内部会话")
@ExtendWith(MockitoExtension.class)
class SessionInnerControllerTest {

	@Mock
	private SessionManagementApplicationService sessionManagementApplicationService;

	@InjectMocks
	private SessionInnerController sessionInnerController;

	@Test
	@DisplayName("批量踢会话：委托 SessionManagementApplicationService")
	void kickAllSessionsShouldDelegateToApplicationService() {
		List<Long> userIds = List.of(2L, 3L);

		sessionInnerController.kickAllSessions(userIds);

		verify(sessionManagementApplicationService).kickAllSessions(userIds);
	}

}
