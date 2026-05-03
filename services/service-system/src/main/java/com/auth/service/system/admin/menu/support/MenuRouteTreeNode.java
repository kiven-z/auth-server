package com.auth.service.system.admin.menu.support;

import com.auth.common.core.model.entity.TreeNode;
import com.auth.service.system.admin.menu.model.entity.SysMenuEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 建树中间节点：承载 {@link SysMenuEntity} 与排序字段，供
 * {@link com.auth.common.core.utils.TreeNodeProcessorUtil} 使用
 *
 * @author Bunny
 */
@Getter
@Setter
public class MenuRouteTreeNode extends TreeNode<MenuRouteTreeNode> implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Integer menuRank;

	private SysMenuEntity menu;

	/**
	 * 由菜单实体构造树节点（id/parentId 与实体一致）
	 * @param entity 菜单行
	 * @return 树节点
	 */
	public static MenuRouteTreeNode fromEntity(SysMenuEntity entity) {
		MenuRouteTreeNode node = new MenuRouteTreeNode();
		node.setId(entity.getId());
		Long parentId = entity.getParentId();
		node.setParentId(parentId == null ? 0L : parentId);
		node.setMenuRank(entity.getMenuRank());
		node.setMenu(entity);
		return node;
	}

}
