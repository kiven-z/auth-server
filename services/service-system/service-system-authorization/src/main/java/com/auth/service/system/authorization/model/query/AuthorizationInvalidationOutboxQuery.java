package com.auth.service.system.authorization.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 授权失效 Outbox 分页查询条件
 *
 * @author Bunny
 */
@Schema(name = "AuthorizationInvalidationOutboxQuery", title = "授权失效 Outbox 查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class AuthorizationInvalidationOutboxQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "业务事件 ID（前缀匹配）")
	private String eventId;

	@Schema(title = "变更维度（AuthorizationChangeKind）")
	private String changeKind;

	@Schema(title = "投递状态（AuthorizationInvalidationOutboxStatus）")
	private String status;

	@Schema(title = "触发模块")
	private String sourceModule;

	@Schema(title = "业务主键（前缀匹配）")
	private String sourceBizId;

	@Schema(title = "创建时间（开始）")
	private LocalDateTime createTimeStart;

	@Schema(title = "创建时间（结束）")
	private LocalDateTime createTimeEnd;

	@Schema(title = "处理完成时间（开始）")
	private LocalDateTime processedAtStart;

	@Schema(title = "处理完成时间（结束）")
	private LocalDateTime processedAtEnd;

}
