package com.auth.service.system.admin.model.vo.menu;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.service.system.admin.model.vo.role.RoleCodeNameRow;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 菜单已分配角色行
 *
 * @author Bunny
 */
@Schema(name = "SysMenuAssignedRoleRow", title = "菜单角色分配行")
@Getter
@Setter
public class SysMenuAssignedRoleRow extends RoleCodeNameRow {

	@JsonStringFormat
	@Schema(title = "角色 ID")
	private Long roleId;

	@Schema(title = "是否已分配给当前菜单")
	private Boolean assigned;

}
