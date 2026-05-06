package com.auth.service.system.message.model.form;

import com.auth.service.system.message.model.EmailTemplateRequireFieldRow;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 仅更新邮件模板 require_fields
 *
 * @author Bunny
 */
@Schema(name = "EmailTemplateRequireFieldsForm", title = "邮件模板变量列表表单")
@Data
public class EmailTemplateRequireFieldsForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "requireFields", title = "变量声明列表（允许空数组）")
	@Valid
	@NotNull(message = "变量列表不能为 null")
	private List<EmailTemplateRequireFieldRow> requireFields;

}
