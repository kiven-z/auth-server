package com.auth.service.auth.controller;

import java.util.List;

import com.auth.common.data.model.PageResponse;
import com.auth.service.auth.model.query.OnlineUserPageQuery;
import com.auth.service.auth.model.vo.OnlineUserPageVO;
import com.auth.service.auth.model.vo.UserSessionVO;
import com.auth.service.auth.service.SessionManagementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AdminSessionController} 单元测试
 */
@DisplayName("AdminSessionController 管理端会话")
@ExtendWith(MockitoExtension.class)
class AdminSessionControllerTest {

	@Mock
	private SessionManagementService sessionManagementService;

	@InjectMocks
	private AdminSessionController adminSessionController;

	@Test
	@DisplayName("分页查询在线用户：委托 SessionManagementService 并返回 VO 分页")
	void pageOnlineUsers_shouldDelegateToSessionManagementService() {
		OnlineUserPageQuery query = new OnlineUserPageQuery();
		query.setPageIndex(2);
		query.setPageSize(10);
		PageResponse<OnlineUserPageVO> page = PageResponse.of(2L, 10L, 0L, List.of());
		when(sessionManagementService.getOnlineUserPage(any(OnlineUserPageQuery.class))).thenReturn(page);

		var result = adminSessionController.pageOnlineUsers(query);

		verify(sessionManagementService).getOnlineUserPage(any(OnlineUserPageQuery.class));
		assertEquals(0L, result.getData().getTotal());
	}

	@Test
	@DisplayName("踢出指定会话：委托 SessionManagementApplicationService")
	void kickShouldDelegateToApplicationService() {
		adminSessionController.kick(1L, "jti-1");

		verify(sessionManagementService).kickSession(1L, "jti-1");
	}

	@Test
	@DisplayName("踢出用户全部会话：委托 SessionManagementApplicationService")
	void kickAllShouldDelegateToApplicationService() {
		adminSessionController.kickAll(1L);

		verify(sessionManagementService).kickAllSessions(1L);
	}

	@Test
	@DisplayName("批量踢出全部会话：委托 SessionManagementApplicationService")
	void kickAllSessionsShouldDelegateToApplicationService() {
		List<Long> userIds = List.of(2L, 3L);

		adminSessionController.kickAllSessions(userIds);

		verify(sessionManagementService).kickAllSessions(userIds);
	}

	@Test
	@DisplayName("查询活跃会话：委托 ApplicationService 并返回映射结果")
	void getListSessionsShouldDelegateAndMapToVo() {
		UserSessionVO sessionVo = new UserSessionVO();
		sessionVo.setUserId(1L);
		sessionVo.setSessionId("jti-1");
		when(sessionManagementService.listActiveSessions(1L)).thenReturn(List.of(sessionVo));

		var result = adminSessionController.getListSessions(1L);

		verify(sessionManagementService).listActiveSessions(1L);
		assertEquals(1, result.getData().size());
		assertEquals("jti-1", result.getData().get(0).getSessionId());
	}

}
