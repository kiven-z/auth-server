package com.auth.module.security.contract.event;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * 操作日志负载（契约层），由切面组装后交给 {@link com.auth.module.security.contract.spi.OperationLogHandler}。
 *
 * @author Bunny
 */
@Value
@Builder
public class OperationLogPayload {

	/**
	 * 操作用户 ID，未登录可为 {@code null}，持久化层应规范为 0。
	 */
	Long userId;

	/**
	 * 操作类型枚举名
	 */
	String operationType;

	/**
	 * 模块编码
	 */
	String module;

	/**
	 * 目标类型
	 */
	String targetType;

	/**
	 * 目标 ID 文本
	 */
	String targetId;

	/**
	 * HTTP 方法
	 */
	String requestMethod;

	/**
	 * 请求 URI
	 */
	String requestUri;

	/**
	 * 请求参数摘要
	 */
	String requestParams;

	/**
	 * 请求体摘要
	 */
	String requestBody;

	/**
	 * HTTP 或业务层感知的响应状态码
	 */
	Integer responseStatus;

	/**
	 * 响应消息摘要
	 */
	String responseMessage;

	/**
	 * 执行耗时毫秒
	 */
	Integer executionTimeMs;

	/**
	 * 变更前 JSON
	 */
	String oldValueJson;

	/**
	 * 变更后 JSON
	 */
	String newValueJson;

	/**
	 * 客户端 IP
	 */
	String ipAddress;

	/**
	 * User-Agent
	 */
	String userAgent;

	/**
	 * 控制器类名
	 */
	String className;

	/**
	 * 控制器方法名
	 */
	String methodName;

	/**
	 * 记录时间
	 */
	@Builder.Default
	Instant timestamp = Instant.now();

}
