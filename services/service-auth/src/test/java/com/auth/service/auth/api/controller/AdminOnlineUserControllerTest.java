package com.auth.service.auth.api.controller;

import com.auth.common.data.model.PageResponse;
import com.auth.service.auth.application.session.OnlineUserQueryApplicationService;
import com.auth.service.auth.domain.session.query.OnlineUserPageQuery;
import com.auth.service.auth.domain.session.vo.OnlineUserPageVO;
import com.auth.service.auth.domain.session.vo.OnlineUserSummaryVO;
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
	private OnlineUserQueryApplicationService onlineUserQueryApplicationService;

	@InjectMocks
	private AdminOnlineUserController adminOnlineUserController;

	@Test
	@DisplayName("分页查询：委托 OnlineUserQueryApplicationService")
	void page_shouldDelegateToApplicationService() {
		OnlineUserPageQuery query = new OnlineUserPageQuery();
		query.setPageIndex(2);
		query.setPageSize(10);
		PageResponse<OnlineUserPageVO> page = PageResponse.of(2L, 10L, 0L, List.of());
		when(onlineUserQueryApplicationService.getOnlineUserPage(query)).thenReturn(page);

		var result = adminOnlineUserController.page(query);

		verify(onlineUserQueryApplicationService).getOnlineUserPage(query);
		assertSame(page, result.getData());
		assertEquals(0L, result.getData().getTotal());
	}

	@Test
	@DisplayName("统计摘要：委托 OnlineUserQueryApplicationService")
	void summary_shouldDelegateToApplicationService() {
		OnlineUserSummaryVO summary = new OnlineUserSummaryVO();
		summary.setOnlineUserCount(5L);
		when(onlineUserQueryApplicationService.getOnlineUserSummary()).thenReturn(summary);

		var result = adminOnlineUserController.summary();

		verify(onlineUserQueryApplicationService).getOnlineUserSummary();
		assertEquals(5L, result.getData().getOnlineUserCount());
	}

}
