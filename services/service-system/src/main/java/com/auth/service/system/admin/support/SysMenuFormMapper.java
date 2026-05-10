package com.auth.service.system.admin.support;

import cn.hutool.core.util.ObjectUtil;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuSaveForm;

/**
 * 菜单保存表单 → 实体（与列表 VO 的 0/1 标志位约定一致）
 *
 * @author Bunny
 */
public final class SysMenuFormMapper {

	private SysMenuFormMapper() {
	}

	private static boolean flagFromApi(Integer v) {
		return v != null && v == 1;
	}

	private static Long normalizeParentIdForStore(Long parentId) {
		if (parentId == null || parentId == 0L) {
			return 0L;
		}
		return parentId;
	}

	/**
	 * 将表单字段写入实体（新建或更新）
	 * @param form 请求体
	 * @param entity 目标实体
	 */
	public static void applyToEntity(SysMenuSaveForm form, SysMenuEntity entity) {
		entity.setParentId(normalizeParentIdForStore(form.getParentId()));
		entity.setMenuType(form.getMenuType());
		entity.setPath(form.getPath());
		entity.setName(form.getName().trim());
		entity.setRedirect(form.getRedirect());
		entity.setComponent(form.getComponent());
		entity.setTitle(form.getTitle().trim());
		entity.setIcon(form.getIcon());
		entity.setShowLink(flagFromApi(form.getShowLink()));
		entity.setMenuRank(ObjectUtil.defaultIfNull(form.getMenuRank(), 0));
		entity.setExtraIcon(form.getExtraIcon());
		entity.setShowParent(form.getShowParent() == null ? Boolean.TRUE : flagFromApi(form.getShowParent()));
		entity.setKeepAlive(flagFromApi(form.getKeepAlive()));
		entity.setFrameSrc(form.getFrameSrc());
		entity.setFrameLoading(form.getFrameLoading() == null ? Boolean.TRUE : flagFromApi(form.getFrameLoading()));
		entity.setTransitionName(form.getTransitionName());
		entity.setEnterTransition(form.getEnterTransition());
		entity.setLeaveTransition(form.getLeaveTransition());
		entity.setHiddenTag(form.getHiddenTag() == null ? Boolean.FALSE : flagFromApi(form.getHiddenTag()));
		entity.setDynamicLevel(form.getDynamicLevel());
		entity.setActivePath(form.getActivePath());
		entity.setFixedTag(form.getFixedTag() == null ? Boolean.FALSE : flagFromApi(form.getFixedTag()));
		// 0=启用 1=禁用
		entity.setStatus(form.getStatus() != null && form.getStatus() == 0);
		entity.setRemark(form.getRemark());
	}

}
