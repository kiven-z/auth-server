package com.auth.service.system.message.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 渠道投递记录分页查询
 *
 * @author Bunny
 */
@Schema(name = "MessageChannelDeliveryQuery", title = "渠道投递记录查询", description = "渠道投递记录分页查询参数")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class MessageChannelDeliveryQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "taskId", title = "任务/批次 ID")
	private Long taskId;

	@Schema(name = "channel", title = "消息渠道", description = "如 EMAIL、SMS、DING_TALK")
	private String channel;

	@Schema(name = "status", title = "投递状态", description = "如 PENDING、SUCCESS、FAILED、SKIPPED")
	private String status;

}
