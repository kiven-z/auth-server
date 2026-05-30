package com.auth.service.system.admin.model.form.emailtemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 仅更新邮件模板正文（content）
 *
 * @author AuthoritySystem
 */
@Schema(name = "EmailTemplateContentForm", title = "邮件模板正文更新")
@Getter
@Setter
public class EmailTemplateContentForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "content", title = "邮件内容(HTML/FreeMarker)", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "邮件内容不能为空")
	private String content;

}
