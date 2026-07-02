package com.auth.service.system.admin.controller.admin;

import java.util.List;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.ip.IpAddressService;
import com.auth.service.system.admin.excel.user.SysUserSpreadsheetService;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.auth.service.system.admin.model.vo.user.SysUserDetailVO;
import com.auth.service.system.admin.model.vo.user.SysUserPageVO;
import com.auth.service.system.admin.model.vo.user.SysUserSearchItemVO;
import com.auth.service.system.admin.service.admin.SysUserQueryService;
import com.auth.service.system.admin.service.admin.SysUserService;
import com.auth.service.system.admin.support.user.UserPasswordService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysUserController} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysUserController 系统用户管理")
@ExtendWith(MockitoExtension.class)
class SysUserControllerTest {

	@Mock
	private SysUserService sysUserService;

	@Mock
	private SysUserQueryService sysUserQueryService;

	@Mock
	private SysUserSpreadsheetService sysUserSpreadsheetService;

	@Mock
	private UserPasswordService userPasswordService;

	@Mock
	private IpAddressService ipAddressService;

	private SysUserController controller;

	@BeforeEach
	void setUp() {
		controller = new SysUserController(sysUserService, sysUserQueryService, sysUserSpreadsheetService,
				userPasswordService, ipAddressService);
	}

	@Test
	@DisplayName("GET /page：委托 SysUserQueryService 并包装 Result")
	void pageDelegatesToService() {
		SysUserPageQuery query = new SysUserPageQuery();
		query.setPageIndex(2);
		query.setPageSize(15);
		PageResponse<SysUserPageVO> pageResponse = PageResponse.of(2L, 15L, 0L, List.of());
		when(sysUserQueryService.getPage(any(Page.class), eq(query))).thenReturn(pageResponse);

		Result<PageResponse<SysUserPageVO>> result = controller.page(query);

		assertThat(result.getData().getList()).isEmpty();
		verify(sysUserQueryService).getPage(any(Page.class), eq(query));
	}

	@Test
	@DisplayName("GET /search：委托 SysUserQueryService 并包装 Result")
	void searchDelegatesToService() {
		SysUserSearchItemVO item = new SysUserSearchItemVO();
		item.setId(9L);
		item.setUsername("nine");
		when(sysUserQueryService.searchByKeyword("ab", 12)).thenReturn(List.of(item));

		Result<List<SysUserSearchItemVO>> result = controller.search("ab", 12);

		assertThat(result.getData()).hasSize(1).first().satisfies(r -> {
			assertThat(r.getId()).isEqualTo(9L);
			assertThat(r.getUsername()).isEqualTo("nine");
		});
		verify(sysUserQueryService).searchByKeyword("ab", 12);
	}

	@Test
	@DisplayName("GET /{userId}/detail：委托 SysUserQueryService 并包装 Result")
	void detailDelegatesToService() {
		SysUserDetailVO detail = new SysUserDetailVO();
		detail.setId(7L);
		detail.setUsername("detail-user");
		when(sysUserQueryService.getDetail(7L)).thenReturn(detail);

		Result<SysUserDetailVO> result = controller.detail(7L);

		assertThat(result.getData().getId()).isEqualTo(7L);
		assertThat(result.getData().getUsername()).isEqualTo("detail-user");
		verify(sysUserQueryService).getDetail(7L);
	}

}
