package com.auth.service.system.admin.model.form.user;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户岗位关联更新表单（不含岗位 ID）
 *
 * @author Bunny
 */
@Schema(name = "UserPostRelationUpdateForm", title = "用户岗位关联更新")
@Getter
@Setter
public class UserPostRelationUpdateForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "关联状态(0=已调离,1=正常,2=兼职)", requiredMode = Schema.RequiredMode.REQUIRED)
	@Max(value = 2, message = "状态值必须 <= 2")
	@Min(value = 0, message = "状态值必须 >= 0")
	@NotNull(message = "状态必填")
	private Integer status;

	@Schema(title = "是否主岗位", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "是否主岗位必填")
	private Boolean isPrimary;

	@Schema(title = "备注")
	@Size(max = 255, message = "备注长度必须 <= 255")
	private String remark;

}
