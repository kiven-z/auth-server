package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.admin.model.vo.user.SysUserSearchItemVO;
import com.auth.service.system.admin.service.admin.SysUserQueryService;
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
 * {@link SysUserQueryController} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysUserQueryController 系统用户查询")
@ExtendWith(MockitoExtension.class)
class SysUserQueryControllerTest {

	@Mock
	private SysUserQueryService sysUserQueryService;

	private SysUserQueryController controller;

	@BeforeEach
	void setUp() {
		controller = new SysUserQueryController(sysUserQueryService);
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

}
