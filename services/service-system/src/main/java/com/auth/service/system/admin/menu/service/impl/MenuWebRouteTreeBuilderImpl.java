package com.auth.service.system.admin.menu.service.impl;

import com.auth.service.system.admin.menu.mapper.SysMenuMapper;
import com.auth.service.system.admin.menu.mapper.SysMenuPermissionMapper;
import com.auth.service.system.admin.menu.mapper.SysMenuRoleMapper;
import com.auth.service.system.admin.menu.model.entity.SysMenuEntity;
import com.auth.service.system.admin.menu.model.vo.RouteNodeVO;
import com.auth.service.system.admin.menu.service.MenuWebRouteTreeBuilder;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 动态菜单路由树构建（启用菜单、角色过滤、祖先补全、排序、VO 映射）
 *
 * @author Bunny
 */
@Slf4j
@Component
public class MenuWebRouteTreeBuilderImpl extends ServiceImpl<SysMenuMapper, SysMenuEntity>
		implements MenuWebRouteTreeBuilder {

	private final SysMenuRoleMapper sysMenuRoleMapper;

	private final SysMenuPermissionMapper sysMenuPermissionMapper;

	public MenuWebRouteTreeBuilderImpl(SysMenuMapper sysMenuMapper, SysMenuRoleMapper sysMenuRoleMapper,
			SysMenuPermissionMapper sysMenuPermissionMapper) {
		this.sysMenuRoleMapper = sysMenuRoleMapper;
		this.sysMenuPermissionMapper = sysMenuPermissionMapper;
	}

	/**
	 * 构建管理员可见的动态路由（顶级节点列表）
	 * @return 路由树根列表
	 */
	@Override
	public List<RouteNodeVO> buildAdminRoutes() {
		List<SysMenuEntity> menuList = list(Wrappers.<SysMenuEntity>lambdaQuery().eq(SysMenuEntity::getStatus, 0));

		return List.of();
	}

	/**
	 * 构建当前用户可见的动态路由（顶级节点列表）
	 * @return 路由树根列表
	 */
	@Override
	public List<RouteNodeVO> buildRoutes() {
		return List.of();
	}

}
