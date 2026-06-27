package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.admin.excel.post.PostSheetImporter;
import com.auth.service.system.admin.excel.post.SysPostSpreadsheetService;
import com.auth.service.system.admin.mapper.post.SysPostMapper;
import com.auth.service.system.admin.model.vo.post.SysPostSearchItemVO;
import com.auth.service.system.admin.service.admin.SysPostQueryService;
import com.auth.service.system.admin.service.admin.SysPostService;
import com.auth.service.system.admin.support.dept.DeptLookupSupport;
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
	private SysPostQueryService sysPostQueryService;

	@Mock
	private SysPostService sysPostService;

	@Mock
	private SysPostMapper sysPostMapper;

	@Mock
	private DeptLookupSupport deptLookupSupport;

	private SysPostController controller;

	@BeforeEach
	void setUp() {
		PostSheetImporter postSheetImporter = new PostSheetImporter(deptLookupSupport, sysPostService);
		SysPostSpreadsheetService sysPostSpreadsheetService = new SysPostSpreadsheetService(sysPostMapper,
				postSheetImporter);
		controller = new SysPostController(sysPostQueryService, sysPostService, sysPostSpreadsheetService);
	}

	@Test
	@DisplayName("GET /search：委托 SysPostQueryService 并包装 Result")
	void searchDelegatesToService() {
		SysPostSearchItemVO item = new SysPostSearchItemVO();
		item.setId(1L);
		item.setPostCode("DEV");
		item.setPostName("研发");
		when(sysPostQueryService.search("DEV", true, 20)).thenReturn(List.of(item));

		Result<List<SysPostSearchItemVO>> result = controller.search("DEV", true, 20);

		assertThat(result.getData()).hasSize(1).first().satisfies(r -> {
			assertThat(r.getId()).isEqualTo(1L);
			assertThat(r.getPostCode()).isEqualTo("DEV");
		});
		verify(sysPostQueryService).search("DEV", true, 20);
	}

}
