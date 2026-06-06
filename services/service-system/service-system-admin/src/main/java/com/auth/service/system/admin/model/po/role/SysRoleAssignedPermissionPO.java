package com.auth.service.system.admin.model.po.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色已分配权限 PO
 *
 * @author Bunny
 */
@Schema(name = "SysRoleAssignedPermissionPO", title = "角色已分配权限 PO")
@Getter
@Setter
public class SysRoleAssignedPermissionPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "权限编码")
	private String permissionCode;

	@Schema(title = "权限名称")
	private String permissionName;

	@Schema(title = "权限类型(0=按钮, 1=API)")
	private Integer permissionType;

	@Schema(title = "启用状态(true=启用，false=停用)")
	private Boolean status;

	@Schema(title = "授权时间")
	private LocalDateTime grantTime;

}
