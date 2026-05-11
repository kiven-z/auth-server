package com.auth.service.system.admin.model.form.emailtemplate;

import com.auth.common.web.valid.group.CreateGroup;
import com.auth.service.system.admin.model.vo.emailtemplate.EmailTemplateRequireFieldRow;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import static com.auth.common.web.model.enums.RegexConstantsEnum.LETTER_UNDERSCORE_HYPHEN_REQUIRE_LETTER;

/**
 * 邮件模板表单
 *
 * @author Bunny
 */
@Schema(name = "EmailTemplateForm", title = "邮件模板表单", description = "用于邮件模板信息的创建与更新")
@Data
public class EmailTemplateForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "id", title = "主键ID", description = "仅创建时不传")
	private Long id;

	@Schema(name = "templateCode", title = "模板编码(唯一)")
	@Pattern(regexp = LETTER_UNDERSCORE_HYPHEN_REQUIRE_LETTER, message = "模板编码只能是字母", groups = CreateGroup.class)
	@NotBlank(message = "模板编码不能为空", groups = CreateGroup.class)
	private String templateCode;

	@Schema(name = "templateName", title = "模板名称(唯一)")
	@NotBlank(message = "模板名称不能为空", groups = CreateGroup.class)
	private String templateName;

	@Schema(name = "subject", title = "邮件主题")
	@NotBlank(message = "邮件主题不能为空", groups = CreateGroup.class)
	private String subject;

	@Schema(name = "description", title = "模板描述")
	private String description;

	@Schema(name = "status", title = "启用状态(true=启用模板，false=停用)")
	@NotNull(message = "状态不能为空", groups = CreateGroup.class)
	private Boolean status;

	@Schema(name = "priority", title = "发送优先级(1-10,数字越大优先级越高)")
	@NotNull(message = "发送优先级不能为空", groups = CreateGroup.class)
	private Integer priority;

	@Schema(name = "content", title = "邮件内容(支持HTML)，可空串，后续在「模板编辑」中维护")
	private String content;

	@Schema(name = "requireFields", title = "模板依赖变量列表（可为空数组）")
	@Valid
	@NotNull(message = "require_fields 不能为 null", groups = CreateGroup.class)
	private List<EmailTemplateRequireFieldRow> requireFields;

}
