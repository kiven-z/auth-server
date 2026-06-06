package com.auth.service.system.admin.service;

import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.vo.menu.RouteNodeVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 将启用菜单、角色过滤与关联表聚合为前端动态路由树
 *
 * @author Bunny
 */
public interface MenuWebRouteTreeBuilder extends IService<SysMenuEntity> {

	/**
	 * 构建管理员可见的动态路由（顶级节点列表）
	 * @param menuList 菜单列表
	 * @return 路由树根列表
	 */
	List<RouteNodeVO> buildAdminRoutes(List<SysMenuEntity> menuList);

	/**
	 * 构建当前用户可见的动态路由（顶级节点列表）
	 * @param menuList 菜单列表
	 * @return 路由树根列表
	 */
	List<RouteNodeVO> buildRoutes(List<SysMenuEntity> menuList);

}
