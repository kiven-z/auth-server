package com.auth.service.system.admin.model.form.emailtemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 邮件模板批量启用/禁用表单
 *
 * @author Bunny
 */
@Schema(name = "EmailTemplateStatusForm", title = "邮件模板批量启用/禁用表单", description = "用于邮件模板批量启用/禁用")
@Getter
@Setter
public class EmailTemplateStatusForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "ids", title = "模板ID列表")
	@NotEmpty(message = "模板ID列表不能为空")
	private List<Long> ids;

	@Schema(name = "status", title = "启用状态(true=启用模板，false=停用)")
	@NotNull(message = "状态不能为空")
	private Boolean status;

}
