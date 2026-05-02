package com.auth.service.system.admin.menu.service;

import com.auth.service.system.admin.menu.model.vo.RouteNodeVO;

import java.util.List;

/**
 * 门户动态路由（当前用户可见菜单树）查询服务。
 *
 * @author Bunny
 */
public interface MenuWebRouteService {

	/**
	 * 查询当前登录用户可见的 Web 动态路由列表（顶级节点列表）。
	 * <p>
	 * 占位阶段返回空列表；后续实现树构建、角色过滤与 meta 填充。
	 * </p>
	 *
	 * @return 顶级路由节点，可能为空
	 */
	List<RouteNodeVO> listWebRoutes();
}
