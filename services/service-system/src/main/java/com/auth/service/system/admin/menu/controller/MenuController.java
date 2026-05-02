package com.auth.service.system.admin.menu.controller;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.admin.menu.model.vo.RouteNodeVO;
import com.auth.service.system.admin.menu.service.MenuWebRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单 Web 端接口（动态路由等）。
 *
 * @author Bunny
 */
@Tag(name = "系统菜单-Web", description = "动态路由（门户）")
@RequestMapping("/api/system/menu")
@RestController
public class MenuController {

	private final MenuWebRouteService menuWebRouteService;

	public MenuController(MenuWebRouteService menuWebRouteService) {
		this.menuWebRouteService = menuWebRouteService;
	}

	/**
	 * 获取当前用户的动态路由树（顶级列表）。
	 *
	 * @return 统一响应包动态路由
	 */
	@Operation(summary = "动态路由列表（占位返回空）")
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/web-routes")
	public Result<List<RouteNodeVO>> getWebRoutes() {
		return Result.success(menuWebRouteService.listWebRoutes());
	}
}
