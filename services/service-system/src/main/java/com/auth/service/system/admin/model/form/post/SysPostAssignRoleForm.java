package com.auth.service.system.admin.model.form.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 岗位分配角色表单 FIXME 我记得这个也是有相同的
 *
 * @author Bunny
 */
@Schema(name = "SysPostAssignRoleForm", title = "岗位分配角色表单")
@Getter
@Setter
public class SysPostAssignRoleForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色编码列表（仅允许 roleType=3/4）", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "roleCodes is required")
	private List<String> roleCodes;

}
