package com.auth.service.auth.session.api.controller;

import com.auth.common.data.model.PageResponse;
import com.auth.service.auth.session.application.OnlineSessionQueryApplicationService;
import com.auth.service.auth.session.domain.model.query.OnlineSessionPageQuery;
import com.auth.service.auth.session.domain.model.vo.OnlineSessionPageVO;
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
 * {@link AdminOnlineSessionController} 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminOnlineSessionController 全局在线会话")
class AdminOnlineSessionControllerTest {

	@Mock
	private OnlineSessionQueryApplicationService onlineSessionQueryApplicationService;

	@InjectMocks
	private AdminOnlineSessionController adminOnlineSessionController;

	@Test
	@DisplayName("分页查询：委托 OnlineSessionQueryApplicationService")
	void page_shouldDelegateToApplicationService() {
		OnlineSessionPageQuery query = new OnlineSessionPageQuery();
		query.setPageIndex(2);
		query.setPageSize(10);
		PageResponse<OnlineSessionPageVO> page = PageResponse.of(2L, 10L, 0L, List.of());
		when(onlineSessionQueryApplicationService.getPage(query)).thenReturn(page);

		var result = adminOnlineSessionController.page(query);

		verify(onlineSessionQueryApplicationService).getPage(query);
		assertSame(page, result.getData());
		assertEquals(0L, result.getData().getTotal());
	}

}
