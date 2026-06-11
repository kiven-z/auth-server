package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.admin.model.vo.post.SysPostSearchItemResponse;
import com.auth.service.system.admin.service.SysPostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysPostController} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysPostController 岗位管理")
@ExtendWith(MockitoExtension.class)
class SysPostControllerTest {

	@Mock
	private SysPostService sysPostService;

	@Mock
	private com.auth.service.system.admin.excel.post.SysPostSpreadsheetService sysPostSpreadsheetService;

	private SysPostController controller;

	@BeforeEach
	void setUp() {
		controller = new SysPostController(sysPostService, sysPostSpreadsheetService);
	}

	@Test
	@DisplayName("GET /search：委托 SysPostService 并包装 Result")
	void searchDelegatesToService() {
		SysPostSearchItemResponse item = new SysPostSearchItemResponse();
		item.setId(1L);
		item.setPostCode("DEV");
		item.setPostName("研发");
		when(sysPostService.search("DEV", true, 20)).thenReturn(List.of(item));

		Result<List<SysPostSearchItemResponse>> result = controller.search("DEV", true, 20);

		assertThat(result.getData()).hasSize(1).first().satisfies(r -> {
			assertThat(r.getId()).isEqualTo(1L);
			assertThat(r.getPostCode()).isEqualTo("DEV");
		});
		verify(sysPostService).search("DEV", true, 20);
	}

}
