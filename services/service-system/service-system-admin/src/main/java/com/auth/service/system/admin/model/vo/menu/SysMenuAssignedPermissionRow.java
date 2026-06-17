package com.auth.service.system.admin.model.vo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜单权限分配行（抽屉：与前端 {@code SysMenuAssignedPermissionRow} 对齐）
 *
 * @author Bunny
 */
@Schema(name = "SysMenuAssignedPermissionRow", title = "菜单权限分配行")
@Getter
@Setter
public class SysMenuAssignedPermissionRow implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "权限编码")
	private String permissionCode;

	@Schema(title = "权限名称")
	private String permissionName;

	@Schema(title = "是否已关联当前菜单")
	private Boolean assigned;

}
