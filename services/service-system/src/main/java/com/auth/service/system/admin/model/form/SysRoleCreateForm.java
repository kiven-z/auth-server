package com.auth.service.system.admin.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色新增表单
 *
 * @author Bunny
 */
@Schema(name = "SysRoleCreateForm", title = "角色新增表单")
@Data
public class SysRoleCreateForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色编码", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 64, message = "角色编码长度不能超过64个字符")
	@NotBlank(message = "角色编码不能为空")
	private String roleCode;

	@Schema(title = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 128, message = "角色名称长度不能超过128个字符")
	@NotBlank(message = "角色名称不能为空")
	private String roleName;

	@Schema(title = "状态(0=正常，1=禁用)", requiredMode = Schema.RequiredMode.REQUIRED)
	@Max(value = 1, message = "状态值无效")
	@Min(value = 0, message = "状态值无效")
	@NotNull(message = "状态不能为空")
	private Integer status;

	@Schema(title = "角色类型(1=系统,2=菜单,3=业务,4=自定义)", requiredMode = Schema.RequiredMode.REQUIRED)
	@Max(value = 4, message = "角色类型值无效")
	@Min(value = 1, message = "角色类型值无效")
	@NotNull(message = "角色类型不能为空")
	private Integer roleType;

	@Schema(title = "显示顺序")
	private Integer orderNum;

	@Schema(title = "备注")
	@Size(max = 500, message = "备注长度不能超过500个字符")
	private String remark;

}
