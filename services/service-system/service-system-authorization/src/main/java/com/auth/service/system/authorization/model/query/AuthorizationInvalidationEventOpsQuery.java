package com.auth.service.system.authorization.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 授权失效幂等事件运维查询条件
 *
 * @author Bunny
 */
@Getter
@Setter
public class AuthorizationInvalidationEventOpsQuery implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "页码")
	private long pageIndex;

	@Schema(title = "每页条数")
	private long pageSize;

	@Schema(title = "业务事件 ID（精确匹配）")
	private String eventId;

	@Schema(title = "变更维度（AuthorizationChangeKind）")
	private String changeKind;

	@Schema(title = "是否处理中占位（impacted_user_count = -1）")
	private Boolean processing;

	@Schema(title = "处理完成时间（开始）")
	private LocalDateTime processedAtStart;

	@Schema(title = "处理完成时间（结束）")
	private LocalDateTime processedAtEnd;

}
