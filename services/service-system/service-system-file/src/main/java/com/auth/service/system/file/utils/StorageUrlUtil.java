package com.auth.service.system.file.utils;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;

/**
 * 存储访问地址工具类
 *
 * @author Bunny
 */
@UtilityClass
public class StorageUrlUtil {

	private static final String HTTP_PREFIX = "http://";

	private static final String HTTPS_PREFIX = "https://";

	/**
	 * 去掉路径前导斜杠
	 * @param value 原始路径
	 * @return 处理后的路径
	 */
	public static String trimLeadingSlash(String value) {
		return CharSequenceUtil.removePrefix(value, "/");
	}

	/**
	 * 去掉地址尾部斜杠
	 * @param value 原始地址
	 * @return 处理后的地址
	 */
	public static String trimTrailingSlash(String value) {
		return CharSequenceUtil.removeSuffix(value, "/");
	}

	/**
	 * 拼接公共访问地址和对象路径
	 * @param publicUrl 访问地址
	 * @param objectPath 对象路径
	 * @return 拼接后的 URL
	 */
	public static String concatPublicUrl(String publicUrl, String objectPath) {
		return trimTrailingSlash(publicUrl) + "/" + trimLeadingSlash(objectPath);
	}

	/**
	 * 规范化 endpoint 为可拼接的 HTTP(S) 基础地址
	 * @param endpoint 原始 endpoint
	 * @return 标准化后的基础地址
	 */
	public static String normalizeHttpBaseUrl(String endpoint) {
		String normalizedEndpoint = trimTrailingSlash(endpoint);
		if (CharSequenceUtil.isBlank(normalizedEndpoint)) {
			return normalizedEndpoint;
		}
		if (CharSequenceUtil.startWithIgnoreCase(normalizedEndpoint, HTTP_PREFIX)
				|| CharSequenceUtil.startWithIgnoreCase(normalizedEndpoint, HTTPS_PREFIX)) {
			return normalizedEndpoint;
		}
		return HTTPS_PREFIX + normalizedEndpoint;
	}

}
