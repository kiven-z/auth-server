package com.auth.service.system.admin.model.form.permission;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 权限新增表单（对齐《权限管理-基础功能》添加字段）
 *
 * @author Bunny
 */
@Schema(name = "SysPermissionCreateForm", title = "权限新增表单")
@Getter
@Setter
public class SysPermissionCreateForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "权限编码（唯一标识）", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "权限编码不能为空")
	@Size(max = 128, message = "权限编码长度不能超过128个字符")
	private String permissionCode;

	@Schema(title = "权限名称", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "权限名称不能为空")
	@Size(max = 128, message = "权限名称长度不能超过128个字符")
	private String permissionName;

	@Schema(title = "权限类型(0=按钮, 1=API)", requiredMode = Schema.RequiredMode.REQUIRED)
	@Min(value = 0, message = "权限类型值无效")
	@Max(value = 1, message = "权限类型值无效")
	@NotNull(message = "权限类型不能为空")
	private Integer permissionType;

	@Schema(title = "显示顺序")
	private Integer orderNum;

	@Schema(title = "启用状态(true=启用，false=停用)；不传时默认启用")
	private Boolean status;

	@Schema(title = "备注")
	@Size(max = 500, message = "备注长度不能超过500个字符")
	private String remark;

}
