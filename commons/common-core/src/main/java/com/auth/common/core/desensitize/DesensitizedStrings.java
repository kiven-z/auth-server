package com.auth.common.core.desensitize;

import java.util.Collections;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;

/**
 * 字符串脱敏工具，供 JSON 序列化与单测复用。
 *
 * @author Bunny
 */
@UtilityClass
public class DesensitizedStrings {

	/**
	 * 按前后保留长度与掩码字符进行自定义脱敏。
	 *
	 * @param value     原始值
	 * @param prefixLen 前缀保留长度
	 * @param suffixLen 后缀保留长度
	 * @param symbol    中间替换字符（通常一位，如 {@code *}）
	 * @return 脱敏后的字符串；空白输入返回空串；长度不足时返回原文
	 */
	public static String custom(String value, int prefixLen, int suffixLen, String symbol) {
		if (CharSequenceUtil.isBlank(value)) {
			return "";
		}
		if (value.length() <= prefixLen + suffixLen) {
			return value;
		}

		String mask = CharSequenceUtil.isBlank(symbol) ? "*" : symbol;
		String prefix = value.substring(0, prefixLen);
		String suffix = value.substring(value.length() - suffixLen);
		String middle = String.join("", Collections.nCopies(value.length() - prefixLen - suffixLen, mask));
		return prefix + middle + suffix;
	}
}
