package com.auth.service.system.admin.model.form.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 部门分配角色表单
 *
 * @author bunny
 */
@Schema(name = "SysDeptAssignRoleForm", title = "部门分配角色表单")
@Getter
@Setter
public class SysDeptAssignRoleForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色编码列表（仅允许 roleType=3/4）", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "roleCodes is required")
	private List<String> roleCodes;

}
