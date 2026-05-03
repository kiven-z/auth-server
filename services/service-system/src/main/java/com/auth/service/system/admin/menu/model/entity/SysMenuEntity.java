package com.auth.service.system.admin.menu.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统菜单（路由）主表，对应 sys_menu。
 *
 * @author Bunny
 */
@TableName("sys_menu")
@Schema(name = "SysMenuEntity", title = "系统菜单")
@Getter
@Setter
public class SysMenuEntity extends BaseEntity {

	@Schema(title = "父菜单ID，0 表示顶级")
	private Long parentId;

	@Schema(title = "菜单类型：0 菜单 1 iframe 2 外链")
	private Integer menuType;

	@Schema(title = "路由路径")
	private String path;

	@Schema(title = "路由名称")
	private String name;

	@Schema(title = "重定向")
	private String redirect;

	@Schema(title = "前端组件路径")
	private String component;

	@Schema(title = "菜单标题")
	private String title;

	@Schema(title = "图标")
	private String icon;

	@Schema(title = "是否在菜单中显示")
	private Boolean showLink;

	@Schema(title = "排序，越大越靠后")
	private Integer menuRank;

	@Schema(title = "额外图标")
	private String extraIcon;

	@Schema(title = "是否显示父级菜单")
	private Boolean showParent;

	@Schema(title = "是否缓存页面")
	private Boolean keepAlive;

	@Schema(title = "iframe 地址")
	private String frameSrc;

	@Schema(title = "iframe 首次加载动画")
	private Boolean frameLoading;

	@Schema(title = "Vue Transition 名称")
	private String transitionName;

	@Schema(title = "进场 animate.css")
	private String enterTransition;

	@Schema(title = "离场 animate.css")
	private String leaveTransition;

	@Schema(title = "禁止加入标签页")
	private Boolean hiddenTag;

	@Schema(title = "动态标签层级")
	private Integer dynamicLevel;

	@Schema(title = "激活路径")
	private String activePath;

	@Schema(title = "固定标签")
	private Boolean fixedTag;

	@Schema(title = "状态：0 启用 1 禁用")
	private Integer status;

	@Schema(title = "扩展 meta JSON")
	private String extraMeta;

	@Schema(title = "逻辑删除：0 否 1 是")
	@TableLogic(value = "0", delval = "1")
	private Boolean isDeleted;

}
