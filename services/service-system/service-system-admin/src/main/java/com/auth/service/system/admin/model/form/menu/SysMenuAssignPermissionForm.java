package com.auth.service.system.admin.model.form.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 菜单分配权限（按钮/UI 权限码）
 *
 * @author Bunny
 */
@Schema(name = "SysMenuAssignPermissionForm", title = "菜单分配权限")
@Getter
@Setter
public class SysMenuAssignPermissionForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "权限编码列表（全量覆盖）", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "permissionCodes is required")
	private List<String> permissionCodes;

}
