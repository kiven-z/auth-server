package com.auth.module.security.autoconfigure.audit;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.auth.common.core.model.response.Result;
import com.auth.common.ip.IpAddressService;
import com.auth.common.ip.IpInfo;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Map;
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

	/**
	 * 请求文本最大长度（与表字段设计对齐，约 4KB）。
	 */
	public static final int MAX_REQUEST_TEXT = 4096;

	private static final int MAX_RESPONSE_MESSAGE = 255;

	private final OperationLogHandler operationLogHandler;

	private final ObjectMapper objectMapper;

	private final IpAddressService ipAddressService;

	public OperationLogAspect(OperationLogHandler operationLogHandler, ObjectMapper objectMapper,
			IpAddressService ipAddressService) {
		this.operationLogHandler = operationLogHandler;
		this.objectMapper = objectMapper;
		this.ipAddressService = ipAddressService;
	}

	/**
	 * 获取当前请求属性
	 * @return 当前请求属性
	 * @throws IllegalStateException 如果当前请求属性为空
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
	 * @param failure 异常
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
	 * 组装请求参数摘要：GET 使用 query string；其它方法序列化 parameterMap。
	 * @param request 请求
	 * @param record 是否记录
	 * @param objectMapper JSON 序列化
	 * @return 摘要或 {@code null}
	 */
	public static String buildRequestParams(HttpServletRequest request, boolean record, ObjectMapper objectMapper) {
		if (!record) {
			return null;
		}
		if ("GET".equalsIgnoreCase(request.getMethod())) {
			// 将 null queryString 转为空串，再统一截取
			String toEmpty = CharSequenceUtil.nullToEmpty(request.getQueryString());
			return CharSequenceUtil.subPre(toEmpty, MAX_REQUEST_TEXT);
		}

		Map<String, String[]> map = request.getParameterMap();
		if (MapUtil.isEmpty(map)) {
			return null;
		}
		try {
			return CharSequenceUtil.subPre(objectMapper.writeValueAsString(map), MAX_REQUEST_TEXT);
		}
		catch (JsonProcessingException ex) {
			return null;
		}
	}

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

	/**
	 * 发布负载
	 * @param joinPoint 连接点
	 * @param meta 元数据
	 * @param result 结果
	 * @param failure 异常
	 * @param startNs 开始时间
	 */
	private void publishPayload(ProceedingJoinPoint joinPoint, OperationLog meta, Object result, Throwable failure,
			long startNs) {
		// 无处理器时跳过
		if (operationLogHandler == null) {
			return;
		}
		// 当前请求属性
		ServletRequestAttributes attributes = currentRequestAttributes();
		if (attributes == null) {
			return;
		}

		// 请求
		HttpServletRequest request = attributes.getRequest();
		// 响应
		HttpServletResponse response = attributes.getResponse();
		// 方法签名
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		// 方法
		Method method = signature.getMethod();

		// 执行时间
		int executionMs = (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
		// 解析HTTP状态码
		Integer httpStatus = resolveHttpStatus(response, failure);
		// 构建响应消息
		String responseMessage = buildResponseMessage(meta, result, failure);
		// 用户ID
		Long userId = SecurityUserUtils.getUserId();
		// 用户代理
		String userAgent = StrUtil.subPre(CharSequenceUtil.nullToEmpty(request.getHeader("User-Agent")), 255);
		// 解析IP地址
		IpInfo ipInfo = ipAddressService.resolveIpInfo(request);
		// 构建负载
		OperationLogPayload payload = OperationLogPayload.builder()
			.userId(userId)
			.operationType(meta.operation().name())
			.module(meta.module())
			.targetType(CharSequenceUtil.nullToEmpty(meta.targetType()))
			.requestMethod(request.getMethod())
			.requestUri(CharSequenceUtil.nullToEmpty(request.getRequestURI()))
			.requestParams(buildRequestParams(request, meta.recordParams(), objectMapper))
			.responseStatus(httpStatus)
			.responseMessage(responseMessage)
			.executionTimeMs(executionMs)
			.ipAddress(ipInfo.getIpAddr())
			.userAgent(userAgent)
			.className(method.getDeclaringClass().getName())
			.methodName(method.getName())
			.build();

		// 处理负载
		try {
			operationLogHandler.handle(payload);
		}
		catch (RuntimeException ex) {
			log.error("OperationLogHandler failed, uri={}, module={}", payload.getRequestUri(), payload.getModule(),
					ex);
		}
	}

	/**
	 * 构建响应消息
	 * @param meta 元数据
	 * @param result 结果
	 * @param failure 异常
	 * @return 响应消息
	 */
	private String buildResponseMessage(OperationLog meta, Object result, Throwable failure) {
		// 如果异常存在记录失败消息
		if (failure != null) {
			return StrUtil.subPre(failure.getMessage(), MAX_RESPONSE_MESSAGE);
		}
		// 如果不需要记录结果消息
		if (!meta.recordResultMessage()) {
			return null;
		}
		// 如果结果是Result类型且消息不为空
		if (result instanceof Result<?> message && CharSequenceUtil.isNotBlank(message.getMessage())) {
			return StrUtil.subPre(message.getMessage(), MAX_RESPONSE_MESSAGE);
		}
		// 上述都不存在
		return null;
	}

}