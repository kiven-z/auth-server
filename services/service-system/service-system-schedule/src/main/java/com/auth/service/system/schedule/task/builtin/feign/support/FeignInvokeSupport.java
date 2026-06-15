package com.auth.service.system.schedule.task.builtin.feign.support;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * 微服务远程调用 URL 与请求头组装
 *
 * @author Bunny
 */
@UtilityClass
public class FeignInvokeSupport {

	/**
	 * 服务发现 URL 前缀
	 */
	private static final String SERVICE_URL_SCHEME = "http://";

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
	 * 将参数 Map 转为 HTTP 请求头
	 * @param headers 请求头 Map
	 * @return HttpHeaders
	 */
	public static @NonNull HttpHeaders toHttpHeaders(Map<String, String> headers) {
		HttpHeaders httpHeaders = new HttpHeaders();
		if (MapUtil.isNotEmpty(headers)) {
			headers.forEach(httpHeaders::set);
		}
		return httpHeaders;
	}

}
