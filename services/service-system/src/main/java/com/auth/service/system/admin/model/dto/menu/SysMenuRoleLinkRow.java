package com.auth.service.system.admin.model.dto.menu;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 菜单与角色编码关联行（持久层查询投影，供动态路由批量加载）
 *
 * @author Bunny
 */
@Schema(name = "SysMenuRoleLinkRow", title = "菜单-角色关联行")
@Data
public class SysMenuRoleLinkRow implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "菜单ID")
	private Long menuId;

	@Schema(title = "角色编码")
	private String roleCode;

}
