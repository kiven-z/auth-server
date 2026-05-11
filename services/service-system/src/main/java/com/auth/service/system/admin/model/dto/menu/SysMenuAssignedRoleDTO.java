package com.auth.service.system.admin.model.dto.menu;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 菜单已分配角色 DTO（Mapper JOIN 查询结果，不与 VO 直接耦合）
 *
 * @author Bunny
 */
@Schema(name = "SysMenuAssignedRoleDTO", title = "菜单已分配角色 DTO")
@Data
public class SysMenuAssignedRoleDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

}
