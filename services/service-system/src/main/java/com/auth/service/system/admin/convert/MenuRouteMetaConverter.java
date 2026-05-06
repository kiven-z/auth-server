package com.auth.service.system.admin.convert;

import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.vo.RouteMetaVO;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct 转换器
 *
 * @author Bunny
 */
@Mapper()
public interface MenuRouteMetaConverter {

	/**
	 * 将菜单实体转为路由元信息 VO
	 * @param menu 菜单实体
	 * @param context 转换上下文（角色 / 权限 Map）
	 * @return 路由元信息 VO
	 */
	@Mapping(target = "rank", source = "menu.menuRank")
	@Mapping(target = "roles",
			expression = "java(context.roleMap().getOrDefault(menu.getId(), java.util.Collections.emptyList()))")
	@Mapping(target = "auths",
			expression = "java(context.permissionMap().getOrDefault(menu.getId(), java.util.Collections.emptyList()))")
	@Mapping(target = "transition",
			expression = "java(com.auth.service.system.admin.menu.support.RouteTransitionVoFactory.fromMenuEntity(menu))")
	RouteMetaVO toRouteMetaVO(SysMenuEntity menu, @Context MenuConvertContext context);

}
