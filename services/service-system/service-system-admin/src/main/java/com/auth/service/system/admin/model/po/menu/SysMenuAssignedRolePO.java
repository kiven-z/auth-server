package com.auth.service.system.admin.model.po.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜单已分配角色 DTO（Mapper JOIN 查询结果，不与 VO 直接耦合）
 *
 * @author Bunny
 */
@Schema(name = "SysMenuAssignedRolePO", title = "菜单已分配角色 PO")
@Getter
@Setter
public class SysMenuAssignedRolePO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色 ID")
	private Long roleId;

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

}
