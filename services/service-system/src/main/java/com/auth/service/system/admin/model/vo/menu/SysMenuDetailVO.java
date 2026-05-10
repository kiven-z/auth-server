package com.auth.service.system.admin.model.vo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 菜单详情（在列表行基础上扩展路由元数据字段，与前端 {@code SysMenuDetailVO} 对齐）
 *
 * @author Bunny
 */
@Schema(name = "SysMenuDetailVO", title = "菜单详情")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMenuDetailVO extends SysMenuListVO {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "重定向")
	private String redirect;

	@Schema(title = "图标")
	private String icon;

	@Schema(title = "额外图标")
	private String extraIcon;

	/**
	 * 0=否 1=是
	 */
	@Schema(title = "是否显示父级菜单：0 否 1 是")
	private Integer showParent;

	@Schema(title = "iframe 地址")
	private String frameSrc;

	/**
	 * 0=否 1=是
	 */
	@Schema(title = "iframe 首次加载动画：0 否 1 是")
	private Integer frameLoading;

	@Schema(title = "Vue Transition 名称")
	private String transitionName;

	@Schema(title = "进场 animate.css")
	private String enterTransition;

	@Schema(title = "离场 animate.css")
	private String leaveTransition;

	/**
	 * 0=否 1=是
	 */
	@Schema(title = "禁止加入标签页：0 否 1 是")
	private Integer hiddenTag;

	@Schema(title = "动态标签层级")
	private Integer dynamicLevel;

	@Schema(title = "激活路径")
	private String activePath;

}
