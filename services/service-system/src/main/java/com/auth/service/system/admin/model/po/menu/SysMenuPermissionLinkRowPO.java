package com.auth.service.system.admin.model.po.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜单与权限编码关联行（持久层查询投影，供动态路由批量加载）
 *
 * @author Bunny
 */
@Schema(name = "SysMenuPermissionLinkRow", title = "菜单-权限关联行")
@Data
public class SysMenuPermissionLinkRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "菜单ID")
	private Long menuId;

	@Schema(title = "权限编码")
	private String permissionCode;

}
