package com.auth.service.system.admin.model.form.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 菜单分配角色
 *
 * @author Bunny
 */
@Schema(name = "SysMenuAssignRoleForm", title = "菜单分配角色")
@Getter
@Setter
public class SysMenuAssignRoleForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色编码列表（全量覆盖）", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "roleCodes is required")
	private List<String> roleCodes;

}
