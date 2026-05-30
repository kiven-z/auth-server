package com.auth.service.system.admin.model.vo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜单已分配角色行（抽屉：与前端 {@code SysMenuAssignedRoleRow} 对齐）
 *
 * @author Bunny
 */
@Schema(name = "SysMenuAssignedRoleRow", title = "菜单角色分配行")
@Getter
@Setter
public class SysMenuAssignedRoleRow implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

	@Schema(title = "是否已分配给当前菜单")
	private Boolean assigned;

}
