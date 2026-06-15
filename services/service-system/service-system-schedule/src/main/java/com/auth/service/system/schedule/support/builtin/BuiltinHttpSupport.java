package com.auth.service.system.schedule.support.builtin;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.text.CharSequenceUtil;
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
 * 内置 HTTP / 负载均衡远程调用工具：请求头、URL 组装与 {@link RestTemplate} 发起请求
 *
 * @author Bunny
 */
@UtilityClass
public class BuiltinHttpSupport {

	/**
	 * 服务发现 URL 前缀（Spring Cloud LoadBalancer 约定）
	 */
	private static final String SERVICE_URL_SCHEME = "http://";

	/**
	 * 将参数 Map 转为 HTTP 请求头
	 * @param headers 请求头 Map
	 * @return HttpHeaders
	 */
	public static @NonNull HttpHeaders toHttpHeaders(@Nullable Map<String, String> headers) {
		HttpHeaders httpHeaders = new HttpHeaders();
		if (headers != null) {
			headers.forEach(httpHeaders::set);
		}
		return httpHeaders;
	}

	/**
	 * 将查询参数拼接到 URL
	 * @param url 原始 URL
	 * @param query 查询参数
	 * @return 拼接后的 URL
	 */
	public static @NonNull String buildUrlWithQuery(@NonNull String url, @Nullable Map<String, String> query) {
		if (query == null || query.isEmpty()) {
			return url;
		}
		UrlBuilder builder = UrlBuilder.ofHttp(url);
		query.forEach(builder::addQuery);
		return builder.build();
	}

	/**
	 * 拼接负载均衡调用地址：{@code http://{serviceName}{path}}
	 * @param serviceName 微服务名
	 * @param path 请求路径
	 * @return 完整 URL
	 */
	public static @NonNull String buildServiceUrl(@NonNull String serviceName, @NonNull String path) {
		String normalizedPath = CharSequenceUtil.startWith(path, '/') ? path : '/' + path;
		return SERVICE_URL_SCHEME + serviceName.trim() + normalizedPath;
	}

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
		HttpHeaders httpHeaders = toHttpHeaders(headers);
		HttpEntity<?> request = hasRequestBody(method) ? new HttpEntity<>(body, httpHeaders)
				: new HttpEntity<>(httpHeaders);
		return restTemplate.exchange(url, method, request, String.class);
	}

	private static boolean hasRequestBody(HttpMethod method) {
		return method == HttpMethod.POST || method == HttpMethod.PUT;
	}

}
