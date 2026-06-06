package com.auth.service.system.admin.support.tree;

import lombok.experimental.UtilityClass;

/**
 * 沿 parent_id 链判断后代关系
 *
 * @author bunny
 */
@UtilityClass
public class TreeParentChainDescendantChecker {

	/**
	 * 判断 nodeId 是否位于 ancestorId 的子树内
	 * @param ancestorId 候选祖先节点
	 * @param nodeId 待判定节点
	 * @param loader 父链加载器
	 * @return nodeId 为 ancestorId 的后代时返回 true
	 */
	public static boolean isDescendantOf(Long ancestorId, Long nodeId, ParentIdLoader loader) {
		Long cursor = nodeId;
		while (cursor != null && cursor > TreeParentIds.ROOT_PARENT_ID) {
			if (cursor.equals(ancestorId)) {
				return true;
			}
			cursor = loader.loadParentId(cursor);
		}
		return false;
	}

	/**
	 * 按节点 ID 加载其 parent_id
	 */
	@FunctionalInterface
	public interface ParentIdLoader {

		/**
		 * 节点 ID 加载其 parent_id
		 * @param nodeId 当前节点 ID
		 * @return 父节点 ID；不存在或无父级时返回 null
		 */
		Long loadParentId(Long nodeId);

	}

}
