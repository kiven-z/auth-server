package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfig.annotation.AuthenticatedApi;
import com.auth.module.security.autoconfig.web.SecurityUserUtils;
import com.auth.service.system.admin.model.vo.RouteNodeVO;
import com.auth.service.system.admin.service.MenuWebRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单 Web 端接口（动态路由等）
 *
 * @author Bunny
 */
@Tag(name = "系统菜单-Web", description = "动态路由（门户）")
@RequestMapping("/api/system/menu")
@RestController
public class MenuWebController {

	private final MenuWebRouteService menuWebRouteService;

	public MenuWebController(MenuWebRouteService menuWebRouteService) {
		this.menuWebRouteService = menuWebRouteService;
	}

	/**
	 * 获取当前用户的动态路由树（顶级列表）
	 * @return 统一响应包动态路由
	 */
	@AuthenticatedApi
	@Operation(summary = "动态路由列表（占位返回空）")
	@GetMapping("/web-routes")
	public Result<List<RouteNodeVO>> getWebRoutes() {
		Long userId = SecurityUserUtils.getUserId();
		List<RouteNodeVO> menuList = menuWebRouteService.listWebRoutes(userId);
		return Result.success(menuList);
	}

}
