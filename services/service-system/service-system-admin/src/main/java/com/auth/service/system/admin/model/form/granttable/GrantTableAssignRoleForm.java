package com.auth.service.system.admin.model.form.granttable;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * grant_table 主体全量覆盖角色表单
 *
 * @author Bunny
 */
@Schema(name = "GrantTableAssignRoleForm", title = "grant_table 分配角色表单")
@Getter
@Setter
public class GrantTableAssignRoleForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色 ID 列表", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "roleIds is required")
	private List<Long> roleIds;

}
