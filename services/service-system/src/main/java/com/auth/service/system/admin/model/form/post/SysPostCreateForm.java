package com.auth.service.system.admin.model.form.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 岗位新增表单
 *
 * @author Bunny
 */
@Schema(name = "SysPostCreateForm", title = "岗位新增表单")
@Data
public class SysPostCreateForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "所属部门ID", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "部门不能为空")
	private Long deptId;

	@Schema(title = "岗位编码", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "岗位编码不能为空")
	@Size(max = 64, message = "岗位编码长度不能超过64个字符")
	private String postCode;

	@Schema(title = "岗位名称", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "岗位名称不能为空")
	@Size(max = 128, message = "岗位名称长度不能超过128个字符")
	private String postName;

	@Schema(title = "状态(0=禁用,1=启用)", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "状态不能为空")
	@Min(value = 0, message = "状态值无效")
	@Max(value = 1, message = "状态值无效")
	private Integer status;

	@Schema(title = "显示顺序")
	private Integer orderNum;

	@Schema(title = "备注")
	@Size(max = 500, message = "备注长度不能超过500个字符")
	private String remark;

}
