package com.auth.service.system.message.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 消息模板分页查询
 *
 * @author Bunny
 */
@Schema(name = "MessageTemplateQuery", title = "消息模板查询", description = "消息模板分页查询参数，channel 必填")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class MessageTemplateQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "channel", title = "消息渠道", description = "如 EMAIL、SMS、DING_TALK、IN_APP",
			requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "消息渠道不能为空")
	private String channel;

	@Schema(name = "templateCode", title = "模板编码")
	private String templateCode;

	@Schema(name = "templateName", title = "模板名称")
	private String templateName;

	@Schema(name = "subject", title = "主题/标题")
	private String subject;

	@Schema(name = "imMessageType", title = "IM/站内信正文类型", description = "TEXT、MARKDOWN、HTML；主要用于 IN_APP")
	private String imMessageType;

	@Schema(name = "status", title = "启用状态(true=启用模板，false=停用)")
	private Boolean status;

}
