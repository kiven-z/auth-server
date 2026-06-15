package com.auth.service.system.schedule.support.builtin;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;
import org.springframework.lang.NonNull;

/**
 * 微服务远程调用 URL 组装
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

}
