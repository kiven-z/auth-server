package com.auth.common.web.resttemplate;

import java.util.Map;

import lombok.experimental.UtilityClass;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

/**
 * {@link RestTemplate} 调用辅助
 *
 * @author Bunny
 */
@UtilityClass
public class RestTemplateUtil {

	/**
	 * 按 HTTP 方法发起调用，响应体按字符串解析
	 * @param restTemplate HTTP 客户端
	 * @param url 请求 URL
	 * @param method HTTP 方法
	 * @param headers 可选请求头
	 * @param body 可选请求体
	 * @return 响应
	 */
	public static ResponseEntity<String> exchange(@NonNull RestTemplate restTemplate, @NonNull String url,
			@NonNull HttpMethod method, @Nullable Map<String, String> headers, @Nullable String body) {
		HttpHeaders httpHeaders = new HttpHeaders();
		if (headers != null) {
			headers.forEach(httpHeaders::set);
		}
		return restTemplate.exchange(url, method, new HttpEntity<>(body, httpHeaders), String.class);
	}

}
