package com.auth.service.system.schedule.support.builtin;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.net.url.UrlBuilder;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * 内置 HTTP 调用通用工具（请求头组装、URL 查询参数拼接等）
 *
 * @author Bunny
 */
@UtilityClass
public class BuiltinHttpSupport {

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

	/**
	 * 将查询参数拼接到 URL
	 * @param url 原始 URL
	 * @param query 查询参数
	 * @return 拼接后的 URL
	 */
	public static @NonNull String buildUrlWithQuery(@NonNull String url, @Nullable Map<String, String> query) {
		if (MapUtil.isEmpty(query)) {
			return url;
		}
		UrlBuilder builder = UrlBuilder.ofHttp(url);
		query.forEach(builder::addQuery);
		return builder.build();
	}

}
