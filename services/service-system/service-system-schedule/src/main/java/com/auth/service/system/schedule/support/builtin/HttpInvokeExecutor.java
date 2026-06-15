package com.auth.service.system.schedule.support.builtin;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 内置 HTTP 任务统一发起请求
 *
 * @author Bunny
 */
@UtilityClass
public class HttpInvokeExecutor {

	/**
	 * 按 HTTP 方法发起调用
	 * @param restTemplate HTTP 客户端
	 * @param url 请求 URL
	 * @param method HTTP 方法
	 * @param headers 可选请求头
	 * @param body 请求体（GET/DELETE 忽略）
	 * @return 响应
	 */
	public static ResponseEntity<String> exchange(@NonNull RestTemplate restTemplate, @NonNull String url,
			@NonNull HttpMethod method, @Nullable Map<String, String> headers, @Nullable String body) {
		HttpHeaders httpHeaders = BuiltinHttpSupport.toHttpHeaders(headers);
		HttpEntity<?> request = hasRequestBody(method) ? new HttpEntity<>(body, httpHeaders)
				: new HttpEntity<>(httpHeaders);
		return restTemplate.exchange(url, method, request, String.class);
	}

	private static boolean hasRequestBody(HttpMethod method) {
		return method == HttpMethod.POST || method == HttpMethod.PUT;
	}

}
