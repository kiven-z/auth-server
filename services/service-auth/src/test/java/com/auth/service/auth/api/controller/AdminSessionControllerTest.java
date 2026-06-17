package com.auth.service.auth.api.controller;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.application.session.command.SessionManagementApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AdminSessionController} 单元测试
 */
@DisplayName("AdminSessionController 管理端会话")
@ExtendWith(MockitoExtension.class)
class AdminSessionControllerTest {

	@Mock
	private SessionManagementApplicationService sessionManagementApplicationService;

	@InjectMocks
	private AdminSessionController adminSessionController;

	@Test
	@DisplayName("踢出指定会话：委托 SessionManagementApplicationService")
	void kickShouldDelegateToApplicationService() {
		adminSessionController.kick(1L, "jti-1");

		verify(sessionManagementApplicationService).kickSession(1L, "jti-1");
	}

	@Test
	@DisplayName("踢出用户全部会话：委托 SessionManagementApplicationService")
	void kickAllShouldDelegateToApplicationService() {
		adminSessionController.kickAll(1L);

		verify(sessionManagementApplicationService).kickAllSessions(1L);
	}

	@Test
	@DisplayName("批量踢出全部会话：委托 SessionManagementApplicationService")
	void kickAllSessionsShouldDelegateToApplicationService() {
		List<Long> userIds = List.of(2L, 3L);

		adminSessionController.kickAllSessions(userIds);

		verify(sessionManagementApplicationService).kickAllSessions(userIds);
	}

	@Test
	@DisplayName("查询活跃会话：委托 ApplicationService 并返回映射结果")
	void getListSessionsShouldDelegateAndMapToVo() {
		UserSessionIndex index = new UserSessionIndex();
		index.setUserId(1L);
		index.setSessionId("jti-1");
		when(sessionManagementApplicationService.listActiveSessions(1L)).thenReturn(List.of(index));

		var result = adminSessionController.getListSessions(1L);

		verify(sessionManagementApplicationService).listActiveSessions(1L);
		assertEquals(1, result.getData().size());
		assertEquals("jti-1", result.getData().get(0).getSessionId());
	}

}
