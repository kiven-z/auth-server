package com.auth.service.system.admin.role.model.form;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 角色状态切换表单
 *
 * @author Bunny
 */
@Schema(name = "SysRoleStatusForm", title = "角色状态切换表单")
@Data
public class SysRoleStatusForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "角色ID不能为空")
	private Long id;

	@Schema(title = "状态(0=正常，1=禁用)", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "状态不能为空")
	@Min(value = 0, message = "状态值无效")
	@Max(value = 1, message = "状态值无效")
	private Integer status;

}
