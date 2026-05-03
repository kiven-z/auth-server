package com.auth.service.system.admin.menu.service;

import java.util.List;

import com.auth.service.system.admin.menu.model.vo.RouteNodeVO;

/**
 * 门户动态路由（当前用户可见菜单树）查询服务
 *
 * @author Bunny
 */
public interface MenuWebRouteService {

	/**
	 * 查询指定用户可见的 Web 动态路由列表（顶级节点列表）
	 * <p>
	 * 内部通过 Feign 拉取 auth 生效编码后再建树（建树逻辑迭代中）
	 * </p>
	 * @param userId 当前用户 ID，null 时返回空列表
	 * @return 顶级路由节点，可能为空
	 */
	List<RouteNodeVO> listWebRoutes(Long userId);

}
