package com.auth.service.system.admin.model.query.log;

import java.io.Serial;
import java.io.Serializable;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 权限决策审计日志分页查询
 *
 * @author Bunny
 */
@Schema(name = "LogAuthorizationAuditQuery", title = "权限决策审计日志查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class LogAuthorizationAuditQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "事件类型：GRANTED / DENIED")
	private String eventType;

	@Schema(title = "决策原因枚举")
	private String decisionReason;

	@Schema(title = "Controller 类名（模糊）")
	private String className;

	@Schema(title = "操作人用户 ID（对应 create_user）")
	private Long createUserId;

}
