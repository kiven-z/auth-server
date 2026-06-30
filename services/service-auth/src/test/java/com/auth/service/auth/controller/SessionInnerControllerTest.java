package com.auth.service.auth.controller;

import java.util.List;

import com.auth.service.auth.model.vo.UserSessionVO;
import com.auth.service.auth.service.SessionManagementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SessionInnerController} 单元测试
 */
@DisplayName("SessionInnerController 内部会话")
@ExtendWith(MockitoExtension.class)
class SessionInnerControllerTest {

	@Mock
	private SessionManagementService sessionManagementService;

	@InjectMocks
	private SessionInnerController sessionInnerController;

	@Test
	@DisplayName("批量踢会话：委托 SessionManagementApplicationService")
	void kickAllSessionsShouldDelegateToApplicationService() {
		List<Long> userIds = List.of(2L, 3L);

		sessionInnerController.kickAllSessions(userIds);

		verify(sessionManagementService).kickAllSessions(userIds);
	}

	@Test
	@DisplayName("查询活跃会话：委托 ApplicationService 并返回映射结果")
	void listActiveSessionsShouldDelegateAndMapToVo() {
		UserSessionVO sessionVo = new UserSessionVO();
		sessionVo.setUserId(1L);
		sessionVo.setSessionId("jti-1");
		when(sessionManagementService.listActiveSessions(1L)).thenReturn(List.of(sessionVo));

		var result = sessionInnerController.listActiveSessions(1L);

		verify(sessionManagementService).listActiveSessions(1L);
		assertEquals(1, result.getData().size());
		assertEquals("jti-1", result.getData().get(0).getSessionId());
	}

	@Test
	@DisplayName("踢出指定会话：委托 SessionManagementApplicationService")
	void kickSessionShouldDelegateToApplicationService() {
		sessionInnerController.kickSession(1L, "jti-1");

		verify(sessionManagementService).kickSession(1L, "jti-1");
	}

}
