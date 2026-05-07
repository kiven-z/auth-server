package com.auth.service.system.admin.service.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.auth.common.core.utils.TreeNodeProcessorUtil;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.system.admin.convert.MenuConvertContext;
import com.auth.service.system.admin.convert.MenuRouteMetaConverter;
import com.auth.service.system.admin.convert.MenuRouteNodeConverter;
import com.auth.service.system.admin.mapper.SysMenuMapper;
import com.auth.service.system.admin.mapper.SysMenuPermissionMapper;
import com.auth.service.system.admin.mapper.SysMenuRoleMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.entity.SysMenuPermissionEntity;
import com.auth.service.system.admin.model.entity.SysMenuRoleEntity;
import com.auth.service.system.admin.model.vo.RouteMetaVO;
import com.auth.service.system.admin.model.vo.RouteNodeVO;
import com.auth.service.system.admin.service.MenuWebRouteTreeBuilder;
import com.auth.service.system.admin.support.MenuRouteTreeNode;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

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

	public MenuWebRouteTreeBuilderImpl(SysMenuRoleMapper sysMenuRoleMapper,
			SysMenuPermissionMapper sysMenuPermissionMapper) {
		this.sysMenuRoleMapper = sysMenuRoleMapper;
		this.sysMenuPermissionMapper = sysMenuPermissionMapper;
	}

	/**
	 * 构建管理员可见的动态路由（顶级节点列表）
	 * <p>
	 * 管理员拥有全部角色与权限，查询所有启用菜单并填充完整的 roles / auths
	 * </p>
	 * @param menuList 已按 status = 启用(true) 过滤的菜单列表
	 * @return 路由树根列表
	 */
	@Override
	public List<RouteNodeVO> buildAdminRoutes(List<SysMenuEntity> menuList) {
		if (CollUtil.isEmpty(menuList)) {
			return Collections.emptyList();
		}

		// 管理员：不用连表，直接使用常量
		Map<Long, List<String>> roleMap = menuList.stream()
			.collect(Collectors.toMap(SysMenuEntity::getId, menu -> PermissionConstant.ADMIN_ROLES, (a, b) -> a));
		Map<Long, List<String>> permissionMap = menuList.stream()
			.collect(Collectors.toMap(SysMenuEntity::getId, menu -> PermissionConstant.ADMIN_WILDCARD_PERMISSIONS,
					(a, b) -> a));
		MenuConvertContext context = new MenuConvertContext(roleMap, permissionMap);
		return buildRouteTree(menuList, context);
	}

	/**
	 * 构建当前用户可见的动态路由（顶级节点列表）
	 * <p>
	 * 当前版本与管理员相同逻辑：返回全部启用菜单并填充 roles / auths， 前端根据用户角色自主判断菜单可见性。 后续可在此方法中按角色过滤菜单列表。
	 * </p>
	 * @param menuList 已按 status = 启用(true) 过滤的菜单列表
	 * @return 路由树根列表
	 */
	@Override
	public List<RouteNodeVO> buildRoutes(List<SysMenuEntity> menuList) {
		if (CollUtil.isEmpty(menuList)) {
			return Collections.emptyList();
		}

		List<Long> menuIds = menuList.stream().map(SysMenuEntity::getId).toList();
		Map<Long, List<String>> roleMap = queryRoleMap(menuIds);
		Map<Long, List<String>> permissionMap = queryPermissionMap(menuIds);
		MenuConvertContext context = new MenuConvertContext(roleMap, permissionMap);

		return buildRouteTree(menuList, context);
	}

	/**
	 * 核心建树方法：批量查询角色 / 权限关联，建树，转为 VO
	 * @param menuList 启用菜单列表
	 * @return 路由树根列表
	 */
	private List<RouteNodeVO> buildRouteTree(List<SysMenuEntity> menuList, MenuConvertContext context) {
		List<MenuRouteTreeNode> nodeList = menuList.stream()
			.map(MenuRouteTreeNode::fromEntity)
			.sorted(Comparator.comparing(MenuRouteTreeNode::getMenuRank,
					Comparator.nullsLast(Comparator.naturalOrder())))
			.toList();
		List<MenuRouteTreeNode> treeNodes = TreeNodeProcessorUtil.process(nodeList);
		return convertTreeToVO(treeNodes, context);
	}

	/**
	 * 批量查询菜单角色关联
	 * @param menuIds 菜单 ID 集合
	 * @return menuId -> roleCode 列表
	 */
	private Map<Long, List<String>> queryRoleMap(List<Long> menuIds) {
		List<SysMenuRoleEntity> menuRoles = sysMenuRoleMapper
			.selectList(Wrappers.<SysMenuRoleEntity>lambdaQuery().in(SysMenuRoleEntity::getMenuId, menuIds));
		return menuRoles.stream()
			.collect(Collectors.groupingBy(SysMenuRoleEntity::getMenuId,
					Collectors.mapping(SysMenuRoleEntity::getRoleCode, Collectors.toList())));
	}

	/**
	 * 批量查询菜单权限关联
	 * @param menuIds 菜单 ID 集合
	 * @return menuId -> permissionCode 列表
	 */
	private Map<Long, List<String>> queryPermissionMap(List<Long> menuIds) {
		List<SysMenuPermissionEntity> menuPermissions = sysMenuPermissionMapper.selectList(
				Wrappers.<SysMenuPermissionEntity>lambdaQuery().in(SysMenuPermissionEntity::getMenuId, menuIds));
		return menuPermissions.stream()
			.collect(Collectors.groupingBy(SysMenuPermissionEntity::getMenuId,
					Collectors.mapping(SysMenuPermissionEntity::getPermissionCode, Collectors.toList())));
	}

	/**
	 * 递归将树节点列表转为 VO 列表
	 * @param nodes 树节点列表
	 * @param context 转换上下文（角色 / 权限 Map）
	 * @return 路由 VO 列表
	 */
	private List<RouteNodeVO> convertTreeToVO(List<MenuRouteTreeNode> nodes, MenuConvertContext context) {
		return nodes.stream().map(node -> {
			RouteMetaVO routeMetaVO = MenuRouteMetaConverter.INSTANCE.toRouteMetaVO(node.getMenu(), context);
			RouteNodeVO vo = MenuRouteNodeConverter.INSTANCE.toRouteNodeVO(node, context);
			vo.setMeta(routeMetaVO);

			if (CollUtil.isNotEmpty(node.getChildren())) {
				List<RouteNodeVO> children = convertTreeToVO(node.getChildren(), context);
				vo.setChildren(children);
			}
			else {
				vo.setChildren(Collections.emptyList());
			}
			return vo;
		}).toList();
	}

}
