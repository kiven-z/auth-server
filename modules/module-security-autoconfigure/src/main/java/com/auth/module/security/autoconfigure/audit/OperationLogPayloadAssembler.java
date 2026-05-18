package com.auth.module.security.autoconfigure.audit;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.ip.IpAddressService;
import com.auth.common.ip.IpInfo;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditOperationModuleKeys;
import com.auth.module.security.contract.context.OperationLogContext;
import com.auth.module.security.contract.event.OperationLogPayloadEvent;
import com.auth.module.security.contract.spi.OperationLogHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 将 Web 请求上下文、控制器方法实参与 {@link OperationLog} 元数据组装为 {@link OperationLogPayloadEvent} 并委托
 * {@link OperationLogHandler}。
 *
 * @author Bunny
 */
@Slf4j
public class OperationLogPayloadAssembler {

	private final OperationLogHandler operationLogHandler;

	private final ObjectMapper objectMapper;

	private final IpAddressService ipAddressService;

	/**
	 * @param operationLogHandler 可为 {@code null}（与切面原行为一致：跳过发布）
	 * @param objectMapper JSON 序列化
	 * @param ipAddressService IP 解析
	 */
	public OperationLogPayloadAssembler(OperationLogHandler operationLogHandler, ObjectMapper objectMapper,
			IpAddressService ipAddressService) {
		this.operationLogHandler = operationLogHandler;
		this.objectMapper = objectMapper;
		this.ipAddressService = ipAddressService;
	}

	/**
	 * 获取当前请求属性
	 * @return 当前请求属性
	 */
	private static ServletRequestAttributes currentRequestAttributes() {
		try {
			return (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		}
		catch (IllegalStateException ex) {
			return null;
		}
	}

	/**
	 * 解析HTTP状态码
	 * @param response 响应
	 * @param failure 方法抛出的异常
	 * @return HTTP状态码
	 */
	private static Integer resolveHttpStatus(HttpServletResponse response, Throwable failure) {
		if (response == null) {
			return failure != null ? 500 : null;
		}
		int status = response.getStatus();
		return (failure != null && status < 400) ? 500 : status;
	}

	/**
	 * 组装并发布操作日志负载。
	 * @param joinPoint 连接点
	 * @param meta 注解元数据
	 * @param result 方法返回值
	 * @param failure 方法抛出的异常
	 * @param startNs 方法开始时间（纳秒）
	 */
	public void assembleAndPublish(ProceedingJoinPoint joinPoint, OperationLog meta, Object result, Throwable failure,
			long startNs) {
		if (operationLogHandler == null) {
			return;
		}
		if (meta == null) {
			log.warn("OperationLog metadata unresolved, skip publishing. target={}",
					joinPoint.getTarget() != null ? joinPoint.getTarget().getClass().getName() : "null");
			return;
		}
		ServletRequestAttributes attributes = currentRequestAttributes();
		if (attributes == null) {
			return;
		}

		HttpServletRequest request = attributes.getRequest();
		HttpServletResponse response = attributes.getResponse();
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();

		OperationLogPayloadEvent payload = buildPayload(new BuildPayloadContext(meta, result, failure, startNs, request,
				response, method, joinPoint.getArgs()));
		dispatch(payload);
	}

	/**
	 * 构建操作日志负载
	 * @param ctx 构建上下文
	 * @return 操作日志负载
	 */
	private OperationLogPayloadEvent buildPayload(BuildPayloadContext ctx) {
		int executionMs = (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - ctx.startNs());
		Integer httpStatus = resolveHttpStatus(ctx.response(), ctx.failure());
		String responseMessage = OperationLogResponseMessageComposer.compose(ctx.meta(), ctx.result(), ctx.failure());
		Long userId = SecurityUserUtils.getUserId();
		String userAgent = CharSequenceUtil.subPre(CharSequenceUtil.nullToEmpty(ctx.request().getHeader("User-Agent")),
				255);
		IpInfo ipInfo = ipAddressService.resolveIpInfo(ctx.request());

		return OperationLogPayloadEvent.builder()
			.userId(userId)
			.operationType(ctx.meta().operation().name())
			.module(AuditOperationModuleKeys.toPersistedModuleKey(ctx.meta().serviceDomain(), ctx.meta().bizModule()))
			.targetType(CharSequenceUtil.nullToEmpty(ctx.meta().targetType()))
			.targetId(OperationLogContext.getTargetId())
			.requestMethod(ctx.request().getMethod())
			.requestUri(CharSequenceUtil.nullToEmpty(ctx.request().getRequestURI()))
			.requestParams(OperationLogRequestParamsBuilder.build(ctx.request(), ctx.meta().recordParams(),
					objectMapper, ctx.methodArgs()))
			.responseStatus(httpStatus)
			.responseMessage(responseMessage)
			.executionTimeMs(executionMs)
			.ipAddress(ipInfo.getIpAddr())
			.userAgent(userAgent)
			.className(ctx.method().getDeclaringClass().getName())
			.methodName(ctx.method().getName())
			.build();
	}

	/**
	 * 分发操作日志负载
	 * @param payload 操作日志负载
	 */
	private void dispatch(OperationLogPayloadEvent payload) {
		try {
			operationLogHandler.handle(payload);
		}
		catch (RuntimeException ex) {
			log.error("OperationLogHandler failed, uri={}, module={}", payload.getRequestUri(), payload.getModule(),
					ex);
		}
	}

}
