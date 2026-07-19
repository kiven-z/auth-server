package com.auth.service.system.message.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 站内信发送任务分页查询
 *
 * @author Bunny
 */
@Schema(name = "InAppSendTaskQuery", title = "站内信发送任务查询", description = "站内信发送任务分页查询参数")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class InAppSendTaskQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "status", title = "任务状态", description = "如 PENDING、SENDING、SUCCESS、PARTIAL、FAILED、RECALLED")
	private String status;

	@Schema(name = "sourceType", title = "来源", description = "如 ADMIN_COMPOSE、TEMPLATE、SYSTEM")
	private String sourceType;

	@Schema(name = "recipientScopeType", title = "接收范围类型", description = "USER / POST / DEPT / ALL")
	private String recipientScopeType;

	@Schema(name = "deliveryMode", title = "投递模式", description = "PULL / PUSH")
	private String deliveryMode;

	@Schema(name = "bizType", title = "业务分类")
	private String bizType;

	@Schema(name = "title", title = "标题关键字", description = "模糊匹配")
	private String title;

}
