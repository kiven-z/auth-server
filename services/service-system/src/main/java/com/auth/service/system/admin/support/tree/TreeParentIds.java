package com.auth.service.system.admin.support.tree;

import lombok.experimental.UtilityClass;

/**
 * 树形数据 parent_id 约定
 *
 * @author bunny
 */
@UtilityClass
public class TreeParentIds {

	/**
	 * 顶级节点 parent_id（与 sys_dept、sys_menu 一致） FIXME 放在 TreeParentIdNormalizer 中
	 */
	public static final long ROOT_PARENT_ID = 0L;

}
