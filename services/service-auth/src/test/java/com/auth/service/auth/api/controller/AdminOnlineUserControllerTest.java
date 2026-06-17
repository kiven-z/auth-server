package com.auth.service.auth.api.controller;

import com.auth.common.data.model.PageResponse;
import com.auth.service.auth.api.dto.session.request.OnlineUserPageQuery;
import com.auth.service.auth.api.dto.session.response.OnlineUserPageVO;
import com.auth.service.auth.application.session.query.OnlineUserQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AdminOnlineUserController} 单元测试
 */
@DisplayName("AdminOnlineUserController 在线用户")
@ExtendWith(MockitoExtension.class)
class AdminOnlineUserControllerTest {

	@Mock
	private OnlineUserQueryService onlineUserQueryService;

	@InjectMocks
	private AdminOnlineUserController adminOnlineUserController;

	@Test
	@DisplayName("分页查询：委托 OnlineUserQueryService 并返回 VO 分页")
	void page_shouldDelegateToApplicationService() {
		OnlineUserPageQuery query = new OnlineUserPageQuery();
		query.setPageIndex(2);
		query.setPageSize(10);
		PageResponse<OnlineUserPageVO> page = PageResponse.of(2L, 10L, 0L, List.of());
		when(onlineUserQueryService.getOnlineUserPage(any(OnlineUserPageQuery.class))).thenReturn(page);

		var result = adminOnlineUserController.page(query);

		verify(onlineUserQueryService).getOnlineUserPage(any(OnlineUserPageQuery.class));
		assertEquals(0L, result.getData().getTotal());
	}

	@Test
	@DisplayName("统计摘要：委托 OnlineUserQueryService 并映射为 VO")
	void summary_shouldDelegateToApplicationService() {
		when(onlineUserQueryService.getOnlineUserCount()).thenReturn(5L);

		var result = adminOnlineUserController.summary();

		verify(onlineUserQueryService).getOnlineUserCount();
		assertEquals(5L, result.getData().getOnlineUserCount());
	}

}
