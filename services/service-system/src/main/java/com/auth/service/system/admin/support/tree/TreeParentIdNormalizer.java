package com.auth.service.system.admin.support.tree;

/**
 * 树节点 parent_id 规范化
 *
 * @author bunny
 */
public class TreeParentIdNormalizer {

	/**
	 * 将 parentId 规范为可比较的父节点 ID（null 或负数视为顶级）
	 * @param parentId 原始父节点 ID
	 * @return 规范化后的父节点 ID
	 */
	public static long normalize(Long parentId) {
		if (parentId == null || parentId < TreeParentIds.ROOT_PARENT_ID) {
			return TreeParentIds.ROOT_PARENT_ID;
		}
		return parentId;
	}

}
