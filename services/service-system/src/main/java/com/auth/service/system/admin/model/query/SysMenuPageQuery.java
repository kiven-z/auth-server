package com.auth.service.system.admin.model.query;

import java.io.Serial;
import java.io.Serializable;

import com.auth.common.core.model.query.PageQueryRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单分页查询条件
 *
 * @author Bunny
 */
@Schema(name = "SysMenuPageQuery", title = "菜单分页查询")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMenuPageQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "路由名称（模糊）")
	private String name;

	@Schema(title = "菜单标题（模糊）")
	private String title;

	@Schema(title = "菜单类型：0 菜单 1 iframe 2 外链")
	private Integer menuType;

	@Schema(title = "状态：0 启用 1 禁用")
	private Integer status;

	@Schema(title = "组件路径（模糊）")
	private String component;

}
