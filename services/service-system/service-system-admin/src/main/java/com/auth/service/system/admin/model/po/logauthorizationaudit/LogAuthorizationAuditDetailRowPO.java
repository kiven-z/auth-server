package com.auth.service.system.admin.model.po.logauthorizationaudit;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 权限决策审计详情行
 *
 * @author Bunny
 */
@Schema(name = "LogAuthorizationAuditDetailRowPO", title = "权限决策审计详情（PO）")
@Getter
@Setter
public class LogAuthorizationAuditDetailRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	@Schema(title = "事件类型：GRANTED / DENIED")
	private String eventType;

	@Schema(title = "会话 ID")
	private String sessionId;

	@Schema(title = "所需权限码（如 sys:user:add）")
	private String requiredPermission;

	@Schema(title = "HTTP 请求方法")
	private String requestMethod;

	@Schema(title = "HTTP 请求 URI")
	private String requestUri;

	@Schema(title = "HTTP 请求 IP")
	private String requestIp;

	@Schema(title = "决策原因")
	private String decisionReason;

	@Schema(title = "决策详情")
	private String decisionDetail;

	@Schema(title = "用户权限摘要")
	private String userPermissionsSummary;

	@Schema(title = "策略编码")
	private String policyCode;

	@Schema(title = "策略决策结果")
	private Boolean policyDecision;

	@Schema(title = "类名")
	private String className;

	@Schema(title = "方法名")
	private String methodName;

	@Schema(title = "方法参数")
	private String methodParams;

	@Schema(title = "异常消息")
	private String exceptionMessage;

	@Schema(title = "备注")
	private String remark;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "创建时间")
	private LocalDateTime createTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "更新时间")
	private LocalDateTime updateTime;

	@Schema(title = "创建用户")
	private Long createUser;

	@Schema(title = "更新用户")
	private Long updateUser;

}
