package com.auth.module.security.contract.dto;

import com.auth.module.security.contract.event.OperationLogPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 操作日志入库请求
 *
 * @author Bunny
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogIngestRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long userId;

	private String operationType;

	private String module;

	private String targetType;

	private String requestMethod;

	private String requestUri;

	private String requestParams;

	private Integer responseStatus;

	private String responseMessage;

	private Integer executionTimeMs;

	private String ipAddress;

	private String userAgent;

	private String className;

	private String methodName;

	private Instant timestamp;

	/**
	 * 从操作日志负载构建入库请求
	 * @param payload 操作日志负载
	 * @return 入库请求
	 */
	public static OperationLogIngestRequest fromPayload(OperationLogPayload payload) {
		return OperationLogIngestRequest.builder()
			.userId(payload.getUserId())
			.operationType(payload.getOperationType())
			.module(payload.getModule())
			.targetType(payload.getTargetType())
			.requestMethod(payload.getRequestMethod())
			.requestUri(payload.getRequestUri())
			.requestParams(payload.getRequestParams())
			.responseStatus(payload.getResponseStatus())
			.responseMessage(payload.getResponseMessage())
			.executionTimeMs(payload.getExecutionTimeMs())
			.ipAddress(payload.getIpAddress())
			.userAgent(payload.getUserAgent())
			.className(payload.getClassName())
			.methodName(payload.getMethodName())
			.timestamp(payload.getTimestamp())
			.build();
	}

	/**
	 * 转换为操作日志负载
	 * @return 操作日志负载
	 */
	public OperationLogPayload toPayload() {
		Instant ts = this.timestamp != null ? this.timestamp : Instant.now();
		return OperationLogPayload.builder()
			.userId(this.userId)
			.operationType(this.operationType)
			.module(this.module)
			.targetType(this.targetType)
			.requestMethod(this.requestMethod)
			.requestUri(this.requestUri)
			.requestParams(this.requestParams)
			.responseStatus(this.responseStatus)
			.responseMessage(this.responseMessage)
			.executionTimeMs(this.executionTimeMs)
			.ipAddress(this.ipAddress)
			.userAgent(this.userAgent)
			.className(this.className)
			.methodName(this.methodName)
			.timestamp(ts)
			.build();
	}

}
