package com.auth.module.security.autoconfigure.audit;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.event.OperationLogPayload;
import com.auth.module.security.contract.spi.OperationLogHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

/**
 * 在带 {@link OperationLog} 的 Web 控制器方法周围组装 {@link OperationLogPayload} 并委托
 * {@link OperationLogHandler}
 *
 * @author Bunny
 */
@Slf4j
@Aspect
public class OperationLogAspect {

	private static final int MAX_RESPONSE_MESSAGE = 255;

	private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();

	private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

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
		if (response == null) {
			return failure != null ? 500 : null;
		}
		int status = response.getStatus();
		if (failure != null && status < 400) {
			return 500;
		}
		return status;
	}

	private static String formatTargetId(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Collection<?> collection) {
			StringJoiner joiner = new StringJoiner(",");
			for (Object o : collection) {
				if (o != null) {
					joiner.add(String.valueOf(o));
				}
			}
			String joined = joiner.toString();
			return CharSequenceUtil.isBlank(joined) ? null : joined;
		}
		return String.valueOf(value);
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
		Object[] args = joinPoint.getArgs();

		int executionMs = (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
		Integer httpStatus = resolveHttpStatus(response, failure);
		String responseMessage = buildResponseMessage(meta, result, failure);

		String targetId = formatTargetId(evalSpel(meta.targetId(), method, args, result));
		String oldJson = toJson(evalSpel(meta.oldValue(), method, args, result));
		String newJson = toJson(evalSpel(meta.newValue(), method, args, result));

		Long userId = SecurityUserUtils.getUserId();
		String userAgent = CharSequenceUtil.nullToEmpty(request.getHeader("User-Agent"));
		userAgent = OperationLogServletSupport.truncate(userAgent, 255);

		OperationLogPayload payload = OperationLogPayload.builder()
			.userId(userId)
			.operationType(meta.operation().name())
			.module(meta.module())
			.targetType(CharSequenceUtil.nullToEmpty(meta.targetType()))
			.targetId(targetId)
			.requestMethod(request.getMethod())
			.requestUri(CharSequenceUtil.nullToEmpty(request.getRequestURI()))
			.requestParams(OperationLogServletSupport.buildRequestParams(request, meta.recordParams(), objectMapper))
			.requestBody(OperationLogServletSupport.readRequestBody(request, meta.recordBody()))
			.responseStatus(httpStatus)
			.responseMessage(responseMessage)
			.executionTimeMs(executionMs)
			.oldValueJson(oldJson)
			.newValueJson(newJson)
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

	private Object evalSpel(String expression, Method method, Object[] args, Object result) {
		if (CharSequenceUtil.isBlank(expression)) {
			return null;
		}
		StandardEvaluationContext ctx = new StandardEvaluationContext();
		ctx.setVariable("result", result);
		String[] names = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
		if (names != null) {
			for (int i = 0; i < names.length && i < args.length; i++) {
				ctx.setVariable(names[i], args[i]);
			}
		}
		for (int i = 0; i < args.length; i++) {
			ctx.setVariable("a" + i, args[i]);
		}
		try {
			return SPEL_PARSER.parseExpression(expression).getValue(ctx);
		}
		catch (RuntimeException ex) {
			log.debug("OperationLog SpEL evaluation skipped, expression={}, reason={}", expression, ex.getMessage());
			return null;
		}
	}

	private String toJson(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof String s) {
			return OperationLogServletSupport.truncate(s, OperationLogServletSupport.MAX_REQUEST_TEXT);
		}
		try {
			return OperationLogServletSupport.truncate(objectMapper.writeValueAsString(value),
					OperationLogServletSupport.MAX_REQUEST_TEXT);
		}
		catch (JsonProcessingException ex) {
			return null;
		}
	}

}
