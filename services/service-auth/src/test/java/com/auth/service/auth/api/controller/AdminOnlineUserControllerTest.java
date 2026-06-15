package com.auth.service.auth.api.controller;

import com.auth.common.data.model.PageResponse;
import com.auth.service.auth.application.session.SessionAdminApplicationService;
import com.auth.service.auth.domain.session.model.query.OnlineUserPageQuery;
import com.auth.service.auth.domain.session.model.vo.OnlineUserPageVO;
import com.auth.service.auth.domain.session.model.vo.OnlineUserSummaryVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AdminOnlineUserController} 单元测试
 */
@DisplayName("AdminOnlineUserController 在线用户")
@ExtendWith(MockitoExtension.class)
class AdminOnlineUserControllerTest {

	@Mock
	private SessionAdminApplicationService sessionAdminApplicationService;

	@InjectMocks
	private AdminOnlineUserController adminOnlineUserController;

	@Test
	@DisplayName("分页查询：委托 SessionAdminApplicationService")
	void page_shouldDelegateToApplicationService() {
		OnlineUserPageQuery query = new OnlineUserPageQuery();
		query.setPageIndex(2);
		query.setPageSize(10);
		PageResponse<OnlineUserPageVO> page = PageResponse.of(2L, 10L, 0L, List.of());
		when(sessionAdminApplicationService.getOnlineUserPage(query)).thenReturn(page);

		var result = adminOnlineUserController.page(query);

		verify(sessionAdminApplicationService).getOnlineUserPage(query);
		assertSame(page, result.getData());
		assertEquals(0L, result.getData().getTotal());
	}

	@Test
	@DisplayName("统计摘要：委托 SessionAdminApplicationService")
	void summary_shouldDelegateToApplicationService() {
		OnlineUserSummaryVO summary = new OnlineUserSummaryVO();
		summary.setOnlineUserCount(5L);
		when(sessionAdminApplicationService.getOnlineUserSummary()).thenReturn(summary);

		var result = adminOnlineUserController.summary();

		verify(sessionAdminApplicationService).getOnlineUserSummary();
		assertEquals(5L, result.getData().getOnlineUserCount());
	}

}
