package com.auth.service.system.message.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 消息模板批量启用/禁用表单
 *
 * @author Bunny
 */
@Schema(name = "MessageTemplateStatusForm", title = "消息模板批量启用/禁用表单")
@Getter
@Setter
public class MessageTemplateStatusForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "ids", title = "模板ID列表")
	@NotEmpty(message = "模板ID列表不能为空")
	private List<Long> ids;

	@Schema(name = "status", title = "启用状态(true=启用模板，false=停用)")
	@NotNull(message = "状态不能为空")
	private Boolean status;

	@Schema(name = "channel", title = "消息渠道")
	@NotBlank(message = "消息渠道不能为空")
	private String channel;

}
