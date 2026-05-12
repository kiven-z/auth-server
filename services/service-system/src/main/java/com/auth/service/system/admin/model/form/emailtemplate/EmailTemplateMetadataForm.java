package com.auth.service.system.admin.model.form.emailtemplate;

import com.auth.common.web.valid.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

import static com.auth.common.web.validation.ValidationPatterns.LETTER_UNDERSCORE_HYPHEN_REQUIRE_LETTER;

/**
 * 邮件模板元数据更新表单（不含 content、require_fields）
 *
 * @author AuthoritySystem
 */
@Schema(name = "EmailTemplateMetadataForm", title = "邮件模板元数据更新", description = "仅更新编码、名称、描述、主题、优先级、状态")
@Data
public class EmailTemplateMetadataForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "id", title = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(groups = UpdateGroup.class, message = "更新时ID不能为空")
	private Long id;

	@Schema(name = "templateCode", title = "模板编码(唯一)")
	@Pattern(regexp = LETTER_UNDERSCORE_HYPHEN_REQUIRE_LETTER, message = "模板编码只能是字母", groups = UpdateGroup.class)
	@NotBlank(message = "模板编码不能为空", groups = UpdateGroup.class)
	private String templateCode;

	@Schema(name = "templateName", title = "模板名称")
	@NotBlank(message = "模板名称不能为空", groups = UpdateGroup.class)
	private String templateName;

	@Schema(name = "subject", title = "邮件主题")
	@NotBlank(message = "邮件主题不能为空", groups = UpdateGroup.class)
	private String subject;

	@Schema(name = "description", title = "模板描述")
	private String description;

	@Schema(name = "status", title = "启用状态(true=启用模板，false=停用)")
	@NotNull(message = "状态不能为空", groups = UpdateGroup.class)
	private Boolean status;

	@Schema(name = "priority", title = "发送优先级(1-10,数字越大优先级越高)")
	@NotNull(message = "发送优先级不能为空", groups = UpdateGroup.class)
	private Integer priority;

}
