package com.auth.service.system.admin.model.vo.emailtemplate;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 邮件模板变量声明（与 require_fields JSON 数组中单条结构一致）
 *
 * @author Bunny
 */
@Schema(name = "EmailTemplateRequireFieldRow", title = "邮件模板变量声明")
@Data
public class EmailTemplateRequireFieldRow implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "key", title = "字段名")
	@NotBlank(message = "变量 key 不能为空")
	private String key;

	@Schema(name = "description", title = "字段含义")
	private String description;

	@Schema(name = "exampleValue", title = "示例数据（可为字符串/数字/数组/对象）")
	@NotNull(message = "示例值 exampleValue 不能为空")
	private transient JsonNode exampleValue;

}
