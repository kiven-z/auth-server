package com.auth.service.system.admin.support;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.vo.menu.SysMenuDetailVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuListVO;

/**
 * 菜单实体 → 列表 VO（0/1 状态位与 parentId 规则与前端一致）
 *
 * @author Bunny
 */
public final class SysMenuListVoConverter {

	private SysMenuListVoConverter() {
	}

	/**
	 * 顶级父节点：库中可能为 null 或 0，统一输出为 null
	 */
	public static Long normalizeParentId(Long parentId) {
		if (parentId == null || parentId == 0L) {
			return null;
		}
		return parentId;
	}

	/**
	 * 启用=true → 0，停用=false → 1
	 */
	public static int statusToApi(Boolean enabled) {
		return Boolean.TRUE.equals(enabled) ? 0 : 1;
	}

	public static int boolFlagToApi(Boolean v) {
		return Boolean.TRUE.equals(v) ? 1 : 0;
	}

	/**
	 * @param entity 持久化实体
	 * @return 列表行 VO
	 */
	public static SysMenuListVO fromEntity(SysMenuEntity entity) {
		if (entity == null) {
			return null;
		}
		SysMenuListVO vo = new SysMenuListVO();
		vo.setId(entity.getId());
		vo.setCreateTime(entity.getCreateTime());
		vo.setUpdateTime(entity.getUpdateTime());
		vo.setCreateUser(entity.getCreateUser());
		vo.setUpdateUser(entity.getUpdateUser());
		vo.setRemark(entity.getRemark());
		vo.setParentId(normalizeParentId(entity.getParentId()));
		vo.setName(entity.getName());
		vo.setTitle(entity.getTitle());
		vo.setPath(entity.getPath());
		vo.setComponent(entity.getComponent());
		vo.setMenuType(entity.getMenuType());
		vo.setStatus(statusToApi(entity.getStatus()));
		vo.setShowLink(boolFlagToApi(entity.getShowLink()));
		vo.setMenuRank(ObjectUtil.defaultIfNull(entity.getMenuRank(), 0));
		vo.setKeepAlive(boolFlagToApi(entity.getKeepAlive()));
		vo.setFixedTag(boolFlagToApi(entity.getFixedTag()));
		return vo;
	}

	/**
	 * @param entity 持久化实体
	 * @return 详情 VO（含扩展元数据字段）
	 */
	public static SysMenuDetailVO toDetailVO(SysMenuEntity entity) {
		if (entity == null) {
			return null;
		}
		SysMenuListVO listVo = fromEntity(entity);
		SysMenuDetailVO vo = new SysMenuDetailVO();
		BeanUtil.copyProperties(listVo, vo);
		vo.setRedirect(entity.getRedirect());
		vo.setIcon(entity.getIcon());
		vo.setExtraIcon(entity.getExtraIcon());
		vo.setShowParent(boolFlagToApi(entity.getShowParent()));
		vo.setFrameSrc(entity.getFrameSrc());
		vo.setFrameLoading(boolFlagToApi(entity.getFrameLoading()));
		vo.setTransitionName(entity.getTransitionName());
		vo.setEnterTransition(entity.getEnterTransition());
		vo.setLeaveTransition(entity.getLeaveTransition());
		vo.setHiddenTag(boolFlagToApi(entity.getHiddenTag()));
		vo.setDynamicLevel(entity.getDynamicLevel());
		vo.setActivePath(entity.getActivePath());
		return vo;
	}

}
