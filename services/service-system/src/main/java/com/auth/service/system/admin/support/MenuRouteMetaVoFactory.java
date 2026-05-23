package com.auth.service.system.admin.support;

import com.auth.service.system.admin.convert.MenuConvertContext;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.vo.menu.RouteMetaVO;
import lombok.experimental.UtilityClass;

import java.util.Collections;

/**
 * 路由 meta VO 构建（菜单实体 + 角色/权限上下文）。
 *
 * @author Bunny
 */
@UtilityClass
public class MenuRouteMetaVoFactory {

	/**
	 * 由菜单实体与关联上下文生成路由 meta。
	 * @param menu 菜单实体
	 * @param context 角色 / 权限聚合上下文
	 * @return 路由 meta；menu 为 null 时返回 null
	 */
	public static RouteMetaVO fromMenuEntity(SysMenuEntity menu, MenuConvertContext context) {
		if (menu == null) {
			return null;
		}

		RouteMetaVO meta = new RouteMetaVO();
		meta.setTitle(menu.getTitle());
		meta.setIcon(menu.getIcon());
		meta.setShowLink(menu.getShowLink());
		meta.setRank(menu.getMenuRank());
		meta.setExtraIcon(menu.getExtraIcon());
		meta.setShowParent(menu.getShowParent());
		meta.setKeepAlive(menu.getKeepAlive());
		meta.setFrameSrc(menu.getFrameSrc());
		meta.setFrameLoading(menu.getFrameLoading());
		meta.setHiddenTag(menu.getHiddenTag());
		meta.setDynamicLevel(menu.getDynamicLevel());
		meta.setActivePath(menu.getActivePath());
		meta.setFixedTag(menu.getFixedTag());

		Long menuId = menu.getId();
		meta.setRoles(context.roleMap().getOrDefault(menuId, Collections.emptyList()));
		meta.setAuths(context.permissionMap().getOrDefault(menuId, Collections.emptyList()));
		meta.setTransition(RouteTransitionVoFactory.fromMenuEntity(menu));
		return meta;
	}

}
