package com.auth.service.system.admin.model.po.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜单已分配权限 DTO（Mapper JOIN 查询结果，不与 VO 直接耦合）
 *
 * @author Bunny
 */
@Schema(name = "SysMenuAssignedPermissionPO", title = "菜单已分配权限 PO")
@Data
public class SysMenuAssignedPermissionPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "权限编码")
	private String permissionCode;

	@Schema(title = "权限名称")
	private String permissionName;

}
