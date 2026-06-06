package com.auth.service.system.admin.support.tree;

import com.auth.service.system.common.exception.SystemBusinessException;
import lombok.experimental.UtilityClass;

/**
 * 树节点移动目标校验（禁止挂到自身或自身后代）
 *
 * @author bunny
 */
@UtilityClass
public class TreeMoveTargetValidator {

	/**
	 * 校验移动目标合法；不合法时抛出调用方提供的业务异常
	 * @param nodeId 被移动节点
	 * @param newParentId 新父节点
	 * @param descendantChecker 后代判定策略
	 * @param moveToSelfException 移动到自身
	 * @param moveToDescendantException 移动到后代
	 */
	public static void requireValid(Long nodeId, Long newParentId, DescendantChecker descendantChecker,
			SystemBusinessException moveToSelfException, SystemBusinessException moveToDescendantException) {
		if (newParentId != null && newParentId.equals(nodeId)) {
			throw moveToSelfException;
		}
		if (newParentId != null && newParentId > TreeParentIds.ROOT_PARENT_ID
				&& descendantChecker.isDescendantOf(nodeId, newParentId)) {
			throw moveToDescendantException;
		}
	}

	@FunctionalInterface
	public interface DescendantChecker {

		/**
		 * 判断 newParentId 是否落在 nodeId 的子树内
		 * @param ancestorId 被移动节点
		 * @param nodeId 候选新父节点
		 * @return nodeId 为 ancestorId 的后代时返回 true
		 */
		boolean isDescendantOf(Long ancestorId, Long nodeId);

	}

}
