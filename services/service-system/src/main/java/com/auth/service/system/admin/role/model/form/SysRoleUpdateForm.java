package com.auth.service.system.admin.role.model.form;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色修改表单
 *
 * @author Bunny
 */
@Schema(name = "SysRoleUpdateForm", title = "角色修改表单")
@Data
public class SysRoleUpdateForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "角色ID不能为空")
	private Long id;

	@Schema(title = "角色编码", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "角色编码不能为空")
	@Size(max = 64, message = "角色编码长度不能超过64个字符")
	private String roleCode;

	@Schema(title = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "角色名称不能为空")
	@Size(max = 128, message = "角色名称长度不能超过128个字符")
	private String roleName;

	@Schema(title = "状态(0=正常，1=禁用)", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "状态不能为空")
	@Min(value = 0, message = "状态值无效")
	@Max(value = 1, message = "状态值无效")
	private Integer status;

	@Schema(title = "角色类型(1=系统,2=菜单,3=业务,4=自定义)", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "角色类型不能为空")
	@Min(value = 1, message = "角色类型值无效")
	@Max(value = 4, message = "角色类型值无效")
	private Integer roleType;

	@Schema(title = "显示顺序")
	private Integer orderNum;

	@Schema(title = "备注")
	@Size(max = 500, message = "备注长度不能超过500个字符")
	private String remark;

}
