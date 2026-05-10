package com.auth.service.system.admin.model.form.menu;

import com.auth.common.web.valid.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜单新增/更新表单（字段与前端 {@code MenuSaveRequest} camelCase 对齐）
 *
 * @author Bunny
 */
@Schema(name = "SysMenuSaveForm", title = "菜单保存表单")
@Data
public class SysMenuSaveForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键，更新时必填")
	@NotNull(groups = UpdateGroup.class, message = "Menu id is required for update")
	private Long id;

	@Schema(title = "父菜单ID，顶级为 null")
	private Long parentId;

	@Schema(title = "菜单类型：0 菜单 1 iframe 2 外链", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "menuType is required")
	private Integer menuType;

	@Schema(title = "路由路径")
	private String path;

	@Schema(title = "路由名称", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "name is required")
	private String name;

	@Schema(title = "重定向")
	private String redirect;

	@Schema(title = "前端组件路径")
	private String component;

	@Schema(title = "菜单标题", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "title is required")
	private String title;

	@Schema(title = "图标")
	private String icon;

	@Schema(title = "是否在菜单中显示：0 否 1 是", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "showLink is required")
	private Boolean showLink;

	@Schema(title = "排序，越大越靠后", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "menuRank is required")
	private Integer menuRank;

	@Schema(title = "额外图标")
	private String extraIcon;

	@Schema(title = "是否显示父级菜单：0 否 1 是")
	private Boolean showParent;

	@Schema(title = "是否缓存：0 否 1 是", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "keepAlive is required")
	private Boolean keepAlive;

	@Schema(title = "iframe 地址")
	private String frameSrc;

	@Schema(title = "iframe 首次加载动画：0 否 1 是")
	private Boolean frameLoading;

	@Schema(title = "Vue Transition 名称")
	private String transitionName;

	@Schema(title = "进场 animate.css")
	private String enterTransition;

	@Schema(title = "离场 animate.css")
	private String leaveTransition;

	@Schema(title = "禁止加入标签页：0 否 1 是")
	private Boolean hiddenTag;

	@Schema(title = "动态标签层级")
	private Integer dynamicLevel;

	@Schema(title = "激活路径")
	private String activePath;

	@Schema(title = "固定标签：0 否 1 是")
	private Boolean fixedTag;

	@Schema(title = "状态：0 启用 1 禁用", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "status is required")
	private Boolean status;

	@Schema(title = "备注")
	private String remark;

}
