package com.auth.service.system.admin.menu.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.admin.menu.feign.AuthInnerPrincipalFeignClient;
import com.auth.service.system.admin.menu.feign.dto.EffectiveCodesVO;
import com.auth.service.system.admin.menu.model.vo.RouteNodeVO;

/**
 * {@link MenuWebRouteServiceImpl} 单元测试。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MenuWebRouteServiceImpl")
class MenuWebRouteServiceImplTest {

	@Mock
	private AuthInnerPrincipalFeignClient authInnerPrincipalFeignClient;

	@Test
	@DisplayName("listWebRoutes(null)：不调用 Feign，返回空列表")
	void listWebRoutes_nullUser_skipsFeign() {
		MenuWebRouteServiceImpl service = new MenuWebRouteServiceImpl(authInnerPrincipalFeignClient);

		List<RouteNodeVO> routes = service.listWebRoutes(null);

		assertNotNull(routes);
		assertTrue(routes.isEmpty());
		verifyNoInteractions(authInnerPrincipalFeignClient);
	}

	@Test
	@DisplayName("listWebRoutes：调用 auth Feign 拉取生效编码，建树前仍返回空列表")
	void listWebRoutes_invokesFeign_returnsEmptyUntilTreeImplemented() {
		EffectiveCodesVO body = new EffectiveCodesVO();
		body.setRoleCodes(List.of("R1"));
		body.setPermissionCodes(List.of("p1"));
		when(authInnerPrincipalFeignClient.getEffectiveCodes(anyLong())).thenReturn(Result.success(body));
		MenuWebRouteServiceImpl service = new MenuWebRouteServiceImpl(authInnerPrincipalFeignClient);

		List<RouteNodeVO> routes = service.listWebRoutes(99L);

		assertNotNull(routes);
		assertTrue(routes.isEmpty());
		verify(authInnerPrincipalFeignClient).getEffectiveCodes(99L);
	}

}
