package com.auth.common.web.valid.sensitive;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;

import java.util.Collections;

/**
 * 敏感信息注解,敏感数据处理工具类
 *
 * @author Bunny
 */
@UtilityClass
public class SensitiveSerializeUtil {

	/**
	 * 自定义格式化内容
	 * @param value 当前值
	 * @param prefixLen 前缀长度
	 * @param suffixLen 后缀长度
	 * @param symbol 格式化的符号 默认 *
	 * @return 格式化完成的值
	 */
	public static String desensitizeCustom(String value, int prefixLen, int suffixLen, String symbol) {
		if (CharSequenceUtil.isBlank(value)) {
			return "";
		}
		if (value.length() <= prefixLen + suffixLen) {
			return value;
		}

		String prefix = value.substring(0, prefixLen);
		String suffix = value.substring(value.length() - suffixLen);
		String middle = String.join("", Collections.nCopies(value.length() - prefixLen - suffixLen, symbol));
		return prefix + middle + suffix;
	}

}
