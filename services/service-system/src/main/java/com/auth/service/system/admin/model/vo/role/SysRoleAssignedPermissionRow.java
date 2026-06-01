package com.auth.service.system.admin.model.vo.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色已分配权限行
 *
 * @author Bunny
 */
@Schema(name = "SysRoleAssignedPermissionRow", title = "角色已分配权限行")
@Getter
@Setter
public class SysRoleAssignedPermissionRow implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "权限编码")
	private String permissionCode;

	@Schema(title = "权限名称")
	private String permissionName;

}
