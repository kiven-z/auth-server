package com.auth.module.security.contract.dto;

import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.module.security.contract.event.SecurityEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * 授权审计负载的传输 DTO（HTTP / Feign / 未来 MQ 消息体），与
 * {@link SecurityAuthorizationAuditPayloadEvent} 字段对齐。
 *
 * @author Bunny
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationAuditIngestRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private SecurityEventType eventType;

	private Long userId;

	private String username;

	private String requestIp;

	private String requestMethod;

	private String requestUri;

	private String requiredAuthority;

	private String decisionReason;

	private String exceptionMessage;

	private String className;

	private String methodName;

	@Builder.Default
	private transient Map<String, Object> methodParams = Collections.emptyMap();

	private Instant timestamp;

	/**
	 * 由 SPI 事件构造传输体（供 auth 侧 Feign 发送）。
	 * @param event 授权审计负载
	 * @return 传输 DTO
	 */
	public static AuthorizationAuditIngestRequest fromEvent(SecurityAuthorizationAuditPayloadEvent event) {
		return AuthorizationAuditIngestRequest.builder()
			.eventType(event.getEventType())
			.userId(event.getUserId())
			.username(event.getUsername())
			.requestIp(event.getRequestIp())
			.requestMethod(event.getRequestMethod())
			.requestUri(event.getRequestUri())
			.requiredAuthority(event.getRequiredAuthority())
			.decisionReason(event.getDecisionReason())
			.exceptionMessage(event.getExceptionMessage())
			.className(event.getClassName())
			.methodName(event.getMethodName())
			.methodParams(event.getMethodParams())
			.timestamp(event.getTimestamp())
			.build();
	}

	/**
	 * 转为契约事件（供 system 落库）。
	 * @return 与持久化逻辑一致的事件对象
	 */
	public SecurityAuthorizationAuditPayloadEvent toPayloadEvent() {
		SecurityEventType type = Objects.requireNonNullElse(this.eventType, SecurityEventType.DENIED);
		Map<String, Object> params = Objects.requireNonNullElse(this.methodParams, Collections.emptyMap());
		Instant ts = Objects.requireNonNullElseGet(this.timestamp, Instant::now);
		return SecurityAuthorizationAuditPayloadEvent.builder()
			.eventType(type)
			.userId(this.userId)
			.username(this.username)
			.requestIp(this.requestIp)
			.requestMethod(this.requestMethod)
			.requestUri(this.requestUri)
			.requiredAuthority(this.requiredAuthority)
			.decisionReason(this.decisionReason)
			.exceptionMessage(this.exceptionMessage)
			.className(this.className)
			.methodName(this.methodName)
			.methodParams(params)
			.timestamp(ts)
			.build();
	}

}
