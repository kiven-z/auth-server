package com.auth.module.security.autoconfigure.audit;

import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 从 Servlet 请求中抽取操作日志所需的 HTTP 侧信息（纯工具，便于单测）
 *
 * @author Bunny
 */
@UtilityClass
public class OperationLogServletSupport {

	/**
	 * 请求文本最大长度（与表字段设计对齐，约 4KB）
	 */
	public static final int MAX_REQUEST_TEXT = 4096;

	/**
	 * 截取文本
	 * @param value 原文
	 * @param maxLen 最大长度
	 * @return 截断后文本
	 */
	public static String truncate(String value, int maxLen) {
		if (CharSequenceUtil.isEmpty(value)) {
			return value;
		}
		return value.length() <= maxLen ? value : value.substring(0, maxLen);
	}

	/**
	 * 解析客户端 IP（优先 {@code X-Forwarded-For} 首段）
	 * @param request 请求
	 * @return IP 文本，非空
	 */
	public static String resolveClientIp(HttpServletRequest request) {
		String xff = request.getHeader("X-Forwarded-For");
		if (CharSequenceUtil.isNotBlank(xff)) {
			int comma = xff.indexOf(',');
			return comma > 0 ? xff.substring(0, comma).trim() : xff.trim();
		}
		return CharSequenceUtil.nullToEmpty(request.getRemoteAddr());
	}

	/**
	 * 组装请求参数摘要：GET 使用 query string；其它方法序列化 parameterMap
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
			return truncate(CharSequenceUtil.nullToEmpty(request.getQueryString()), MAX_REQUEST_TEXT);
		}
		Map<String, String[]> map = request.getParameterMap();
		if (map == null || map.isEmpty()) {
			return null;
		}
		try {
			return truncate(objectMapper.writeValueAsString(map), MAX_REQUEST_TEXT);
		}
		catch (JsonProcessingException ex) {
			return null;
		}
	}

	/**
	 * 读取可缓存的请求体（需上游包装 {@link ContentCachingRequestWrapper}）
	 * @param request 请求
	 * @param want 是否尝试读取
	 * @return 正文或 {@code null}
	 */
	public static String readRequestBody(HttpServletRequest request, boolean want) {
		if (!want) {
			return null;
		}
		if (request instanceof ContentCachingRequestWrapper wrapper) {
			byte[] buf = wrapper.getContentAsByteArray();
			if (buf == null || buf.length == 0) {
				return null;
			}
			return truncate(new String(buf, StandardCharsets.UTF_8), MAX_REQUEST_TEXT);
		}
		return null;
	}

}
