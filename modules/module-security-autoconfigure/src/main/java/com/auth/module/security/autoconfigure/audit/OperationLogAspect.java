package com.auth.module.security.autoconfigure.audit;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.event.OperationLogPayload;
import com.auth.module.security.contract.spi.OperationLogHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 在带 {@link OperationLog} 的 Web 控制器方法周围组装 {@link OperationLogPayload} 并委托
 * {@link OperationLogHandler}。
 *
 * @author Bunny
 */
@Slf4j
@Aspect
public class OperationLogAspect {

	private static final int MAX_RESPONSE_MESSAGE = 255;

	private final OperationLogHandler operationLogHandler;

	private final ObjectMapper objectMapper;

	public OperationLogAspect(OperationLogHandler operationLogHandler, ObjectMapper objectMapper) {
		this.operationLogHandler = operationLogHandler;
		this.objectMapper = objectMapper;
	}

	private static ServletRequestAttributes currentRequestAttributes() {
		try {
			return (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		}
		catch (IllegalStateException ex) {
			return null;
		}
	}

	private static Integer resolveHttpStatus(HttpServletResponse response, Throwable failure) {
		// FIXME 解析问题
		if (response == null) {
			return failure != null ? 500 : null;
		}
		int status = response.getStatus();
		if (failure != null && status < 400) {
			return 500;
		}
		return status;
	}

	/**
	 * 环绕通知：采集 HTTP 与返回值上下文后写入操作日志
	 * @param joinPoint 连接点
	 * @return 原方法返回值
	 * @throws Throwable 原方法异常
	 */
	@Around("@annotation(operationLog) || @within(operationLog)")
	public Object aroundControllerMethod(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
		long startNs = System.nanoTime();
		Object result = null;
		Throwable failure = null;
		try {
			result = joinPoint.proceed();
			return result;
		}
		catch (Throwable ex) {
			failure = ex;
			throw ex;
		}
		finally {
			publishPayload(joinPoint, operationLog, result, failure, startNs);
		}
	}

	private void publishPayload(ProceedingJoinPoint joinPoint, OperationLog meta, Object result, Throwable failure,
			long startNs) {
		ServletRequestAttributes attributes = currentRequestAttributes();
		if (attributes == null) {
			return;
		}
		HttpServletRequest request = attributes.getRequest();
		HttpServletResponse response = attributes.getResponse();
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();

		int executionMs = (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
		Integer httpStatus = resolveHttpStatus(response, failure);
		String responseMessage = buildResponseMessage(meta, result, failure);

		Long userId = SecurityUserUtils.getUserId();
		String userAgent = CharSequenceUtil.nullToEmpty(request.getHeader("User-Agent"));
		userAgent = OperationLogServletSupport.truncate(userAgent, 255);

		OperationLogPayload payload = OperationLogPayload.builder()
			.userId(userId)
			.operationType(meta.operation().name())
			.module(meta.module())
			.targetType(CharSequenceUtil.nullToEmpty(meta.targetType()))
			.requestMethod(request.getMethod())
			.requestUri(CharSequenceUtil.nullToEmpty(request.getRequestURI()))
			.requestParams(OperationLogServletSupport.buildRequestParams(request, meta.recordParams(), objectMapper))
			.responseStatus(httpStatus)
			.responseMessage(responseMessage)
			.executionTimeMs(executionMs)
			.ipAddress(OperationLogServletSupport.resolveClientIp(request))
			.userAgent(userAgent)
			.className(method.getDeclaringClass().getName())
			.methodName(method.getName())
			.build();

		try {
			operationLogHandler.handle(payload);
		}
		catch (RuntimeException ex) {
			log.error("OperationLogHandler failed, uri={}, module={}", payload.getRequestUri(), payload.getModule(),
					ex);
		}
	}

	private String buildResponseMessage(OperationLog meta, Object result, Throwable failure) {
		if (failure != null) {
			return OperationLogServletSupport.truncate(failure.getMessage(), MAX_RESPONSE_MESSAGE);
		}
		if (!meta.recordResultMessage()) {
			return null;
		}
		if (result instanceof Result<?> r && CharSequenceUtil.isNotBlank(r.getMessage())) {
			return OperationLogServletSupport.truncate(r.getMessage(), MAX_RESPONSE_MESSAGE);
		}
		return null;
	}

}
