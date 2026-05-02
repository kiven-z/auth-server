package com.auth.service.system.admin.menu.service.impl;

import com.auth.service.system.admin.menu.model.vo.RouteNodeVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MenuWebRouteServiceImpl} 占位行为单元测试。
 */
@DisplayName("MenuWebRouteServiceImpl")
class MenuWebRouteServiceImplTest {

	@Test
	@DisplayName("listWebRoutes：占位阶段返回非 null 的空列表")
	void listWebRoutes_placeholder_returnsEmptyList() {
		MenuWebRouteServiceImpl service = new MenuWebRouteServiceImpl();

		List<RouteNodeVO> routes = service.listWebRoutes();

		assertNotNull(routes);
		// 占位实现：不应返回任何路由节点，便于后续替换为真实树数据
		assertTrue(routes.isEmpty());
	}
}
