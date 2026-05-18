package com.auth.service.system.admin.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.vo.menu.RouteTransitionVO;
import lombok.experimental.UtilityClass;

/**
 * 路由过渡动画 VO 构建（与 pure-admin animate.css 前缀约定一致）
 *
 * @author Bunny
 */
@UtilityClass
public class RouteTransitionVoFactory {

	/**
	 * 由菜单实体生成 meta.transition
	 * @param entity 菜单
	 * @return 过渡 VO，字段均可为空
	 */
	public static RouteTransitionVO fromMenuEntity(SysMenuEntity entity) {
		if (entity == null) {
			return null;
		}
		RouteTransitionVO vo = new RouteTransitionVO();
		vo.setName(processTransitionName(entity.getTransitionName()));
		vo.setEnterTransition(processAnimateValue(entity.getEnterTransition()));
		vo.setLeaveTransition(processAnimateValue(entity.getLeaveTransition()));
		boolean blank = CharSequenceUtil.isBlank(vo.getName()) && CharSequenceUtil.isBlank(vo.getEnterTransition())
				&& CharSequenceUtil.isBlank(vo.getLeaveTransition());
		return blank ? null : vo;
	}

	/**
	 * 处理过渡名称
	 * @param name 过渡名称
	 * @return 处理后的过渡名称
	 */
	private static String processTransitionName(String name) {
		if (CharSequenceUtil.isBlank(name)) {
			return name;
		}
		return "animate__animated animate__" + name + " ";
	}

	/**
	 * 处理过渡值
	 * @param value 过渡值
	 * @return 处理后的过渡值
	 */
	private static String processAnimateValue(String value) {
		if (CharSequenceUtil.isBlank(value)) {
			return value;
		}
		return "animate__" + value;
	}

}
