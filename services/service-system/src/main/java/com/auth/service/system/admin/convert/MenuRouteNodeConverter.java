package com.auth.service.system.admin.convert;

import com.auth.service.system.admin.model.vo.menu.RouteNodeVO;
import com.auth.service.system.admin.support.MenuRouteTreeNode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct 转换器
 * <p>
 * children 字段由调用方递归赋值，此处置空
 * </p>
 *
 * @author Bunny
 */
@Mapper
public interface MenuRouteNodeConverter {

	MenuRouteNodeConverter INSTANCE = Mappers.getMapper(MenuRouteNodeConverter.class);

	/**
	 * 将树节点转为路由 VO（单层，不含 children 递归）
	 * @param node 建树中间节点
	 * @return 路由 VO（meta 由
	 * {@link com.auth.service.system.admin.support.MenuRouteMetaVoFactory} 填充）
	 */
	@Mapping(target = "path", source = "node.menu.path")
	@Mapping(target = "name", source = "node.menu.name")
	@Mapping(target = "redirect", source = "node.menu.redirect")
	@Mapping(target = "component", source = "node.menu.component")
	@Mapping(target = "meta", ignore = true)
	@Mapping(target = "children", ignore = true)
	RouteNodeVO toRouteNodeVO(MenuRouteTreeNode node);

}
