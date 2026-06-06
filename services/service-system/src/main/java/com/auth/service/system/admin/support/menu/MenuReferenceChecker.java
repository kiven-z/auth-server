package com.auth.service.system.admin.support.menu;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysMenuMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.support.tree.TreeMoveTargetValidator;
import com.auth.service.system.admin.support.tree.TreeParentChainDescendantChecker;
import com.auth.service.system.admin.support.tree.TreeParentIdNormalizer;
import com.auth.service.system.admin.support.tree.TreeParentIds;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.springframework.stereotype.Component;

/**
 * 菜单写操作前的存在性与移动目标校验。
 *
 * @author bunny
 */
@Component
public class MenuReferenceChecker {

	private final SysMenuMapper sysMenuMapper;

	public MenuReferenceChecker(SysMenuMapper sysMenuMapper) {
		this.sysMenuMapper = sysMenuMapper;
	}

	/**
	 * 将 parentId 规范为可比较的父菜单 ID（null 或负数视为顶级）。
	 * @param parentId 原始父菜单 ID
	 * @return 规范化后的父菜单 ID
	 */
	public static long normalizeParentId(Long parentId) {
		return TreeParentIdNormalizer.normalize(parentId);
	}

	/**
	 * 读取已存在菜单，不存在时抛出业务异常。
	 * @param menuId 菜单 ID
	 * @return 菜单实体
	 */
	public SysMenuEntity getExisting(Long menuId) {
		SysMenuEntity existing = sysMenuMapper.selectById(menuId);
		if (existing == null) {
			throw new SystemBusinessException(SystemAdminResultCode.MENU_NOT_FOUND, menuId);
		}
		return existing;
	}

	/**
	 * 规范化父菜单 ID 并校验父菜单存在（顶级跳过校验）。
	 * @param rawParentId 原始父菜单 ID
	 * @return 规范化后的父菜单 ID
	 */
	public long normalizeAndRequireParent(Long rawParentId) {
		long parentId = normalizeParentId(rawParentId);
		if (parentId > TreeParentIds.ROOT_PARENT_ID && sysMenuMapper.selectById(parentId) == null) {
			throw new SystemBusinessException(SystemAdminResultCode.MENU_PARENT_NOT_FOUND, parentId);
		}
		return parentId;
	}

	/**
	 * 移动目标不能是自身或自身后代。
	 * @param menuId 被移动菜单
	 * @param newParentId 新父菜单
	 */
	public void requireMoveTargetValid(Long menuId, Long newParentId) {
		TreeMoveTargetValidator.requireValid(menuId, newParentId, this::isDescendantOf,
				new SystemBusinessException(SystemAdminResultCode.MENU_MOVE_TO_SELF, menuId),
				new SystemBusinessException(SystemAdminResultCode.MENU_MOVE_TO_DESCENDANT, menuId, newParentId));
	}

	private boolean isDescendantOf(Long ancestorId, Long nodeId) {
		return TreeParentChainDescendantChecker.isDescendantOf(ancestorId, nodeId, id -> {
			SysMenuEntity node = sysMenuMapper.selectById(id);
			return node == null ? null : node.getParentId();
		});
	}

}
