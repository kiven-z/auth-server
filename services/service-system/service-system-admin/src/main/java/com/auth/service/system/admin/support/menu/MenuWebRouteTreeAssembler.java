package com.auth.service.system.admin.support.menu;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.utils.TreeNodeProcessorUtil;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.system.admin.convert.menu.MenuConvertContext;
import com.auth.service.system.admin.convert.menu.MenuRouteNodeConverter;
import com.auth.service.system.admin.mapper.menu.SysMenuRoleMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.po.menu.SysMenuRoleLinkRowPO;
import com.auth.service.system.admin.model.vo.menu.RouteMetaVO;
import com.auth.service.system.admin.model.vo.menu.RouteNodeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 动态菜单路由树组装（启用菜单、角色过滤、祖先补全、排序、VO 映射）
 *
 * @author Bunny
 */
@Slf4j
@Component
public class MenuWebRouteTreeAssembler {

	private final SysMenuRoleMapper sysMenuRoleMapper;

	public MenuWebRouteTreeAssembler(SysMenuRoleMapper sysMenuRoleMapper) {
		this.sysMenuRoleMapper = sysMenuRoleMapper;
	}

	/**
	 * 构建管理员可见的动态路由（顶级节点列表）
	 * @param menuList 菜单列表
	 * @return 路由树根列表
	 */
	public List<RouteNodeVO> buildAdminRoutes(List<SysMenuEntity> menuList) {
		if (CollUtil.isEmpty(menuList)) {
			return Collections.emptyList();
		}

		Map<Long, List<String>> roleMap = menuList.stream()
			.collect(Collectors.toMap(SysMenuEntity::getId, menu -> PermissionConstant.ADMIN_ROLES, (a, b) -> a));
		MenuConvertContext context = MenuConvertContext.builder().roleMap(roleMap).build();
		return buildRouteTree(menuList, context);
	}

	/**
	 * 构建当前用户可见的动态路由（顶级节点列表）
	 * @param menuList 菜单列表
	 * @return 路由树根列表
	 */
	public List<RouteNodeVO> buildRoutes(List<SysMenuEntity> menuList) {
		if (CollUtil.isEmpty(menuList)) {
			return Collections.emptyList();
		}

		List<Long> menuIds = menuList.stream().map(SysMenuEntity::getId).toList();
		Map<Long, List<String>> roleMap = queryRoleMap(menuIds);
		MenuConvertContext context = MenuConvertContext.builder().roleMap(roleMap).build();

		return buildRouteTree(menuList, context);
	}

	/**
	 * 核心建树方法：批量查询角色关联，建树，转为 VO
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
		if (CollUtil.isEmpty(menuIds)) {
			return Collections.emptyMap();
		}

		List<SysMenuRoleLinkRowPO> menuRoles = sysMenuRoleMapper.selectActiveRoleLinksByMenuIds(menuIds);
		return menuRoles.stream()
			.collect(Collectors.groupingBy(SysMenuRoleLinkRowPO::getMenuId,
					Collectors.mapping(SysMenuRoleLinkRowPO::getRoleCode, Collectors.toList())));
	}

	/**
	 * 递归将树节点列表转为 VO 列表
	 * @param nodes 树节点列表
	 * @param context 转换上下文（角色 Map）
	 * @return 路由 VO 列表
	 */
	private List<RouteNodeVO> convertTreeToVO(List<MenuRouteTreeNode> nodes, MenuConvertContext context) {
		return nodes.stream().map(node -> {
			RouteMetaVO routeMetaVO = MenuRouteMetaVoFactory.fromMenuEntity(node.getMenu(), context);
			RouteNodeVO vo = MenuRouteNodeConverter.INSTANCE.toRouteNodeVO(node);
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
