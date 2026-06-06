package com.auth.service.system.admin.support.menu;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysMenuMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.springframework.stereotype.Component;

/**
 * 菜单写操作前的存在性与移动目标校验。
 *
 * @author bunny
 */
@Component
public class MenuReferenceChecker {

    /**
     * 顶级菜单 parent_id
     */
    public static final long ROOT_PARENT_ID = 0L;

    private final SysMenuMapper sysMenuMapper;

    public MenuReferenceChecker(SysMenuMapper sysMenuMapper) {
        this.sysMenuMapper = sysMenuMapper;
    }

    /**
     * 将 parentId 规范为可比较的父菜单 ID（null 或负数视为顶级）。
     *
     * @param parentId 原始父菜单 ID
     * @return 规范化后的父菜单 ID
     */
    public static long normalizeParentId(Long parentId) {
        if (parentId == null || parentId < ROOT_PARENT_ID) {
            return ROOT_PARENT_ID;
        }
        return parentId;
    }

    /**
     * 读取已存在菜单，不存在时抛出业务异常。
     *
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
     *
     * @param rawParentId 原始父菜单 ID
     * @return 规范化后的父菜单 ID
     */
    public long normalizeAndRequireParent(Long rawParentId) {
        long parentId = normalizeParentId(rawParentId);
        if (parentId > ROOT_PARENT_ID && sysMenuMapper.selectById(parentId) == null) {
            throw new SystemBusinessException(SystemAdminResultCode.MENU_PARENT_NOT_FOUND, parentId);
        }
        return parentId;
    }

    /**
     * 移动目标不能是自身或自身后代。
     *
     * @param menuId      被移动菜单
     * @param newParentId 新父菜单
     */
    public void requireMoveTargetValid(Long menuId, Long newParentId) {
        if (newParentId != null && newParentId.equals(menuId)) {
            throw new SystemBusinessException(SystemAdminResultCode.MENU_MOVE_TO_SELF, menuId);
        }
        if (newParentId != null && newParentId > ROOT_PARENT_ID && isAncestor(menuId, newParentId)) {
            throw new SystemBusinessException(SystemAdminResultCode.MENU_MOVE_TO_DESCENDANT, menuId, newParentId);
        }
    }

    private boolean isAncestor(Long ancestorId, Long nodeId) {
        Long cursor = nodeId;
        while (cursor != null && cursor > ROOT_PARENT_ID) {
            if (cursor.equals(ancestorId)) {
                return true;
            }
            SysMenuEntity node = sysMenuMapper.selectById(cursor);
            if (node == null) {
                return false;
            }
            cursor = node.getParentId();
        }
        return false;
    }

}
