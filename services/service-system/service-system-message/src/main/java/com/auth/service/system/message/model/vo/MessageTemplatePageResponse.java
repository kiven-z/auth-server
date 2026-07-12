package com.auth.service.system.message.model.vo;

import java.io.Serial;
import java.io.Serializable;

import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 消息模板分页返回对象
 *
 * @author Bunny
 */
@Schema(name = "MessageTemplatePageResponse", title = "消息模板分页返回对象", description = "用于消息模板分页查询返回数据")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class MessageTemplatePageResponse extends BaseResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "channel", title = "消息渠道")
	private String channel;

	@Schema(name = "templateCode", title = "模板编码")
	private String templateCode;

	@Schema(name = "templateName", title = "模板名称")
	private String templateName;

	@Schema(name = "subject", title = "主题/标题")
	private String subject;

	@Schema(name = "priority", title = "发送优先级")
	private Integer priority;

	@Schema(title = "启用状态(true=启用模板，false=停用)")
	private Boolean status;

	@Schema(name = "description", title = "模板描述")
	private String description;

}
