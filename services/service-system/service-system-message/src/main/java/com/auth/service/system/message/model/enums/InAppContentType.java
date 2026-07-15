package com.auth.service.system.message.model.enums;

import cn.hutool.core.text.CharSequenceUtil;

/**
 * 站内信正文类型
 *
 * @author Bunny
 */
public enum InAppContentType {

	/**
	 * 纯文本
	 */
	TEXT,

	/**
	 * Markdown
	 */
	MARKDOWN,;

	/**
	 * 解析正文类型；空或未知时按 TEXT
	 * @param raw 原始字符串
	 * @return 正文类型
	 */
	public static InAppContentType from(String raw) {
		if (CharSequenceUtil.isBlank(raw)) {
			return TEXT;
		}
		try {
			return InAppContentType.valueOf(raw.trim().toUpperCase());
		}
		catch (IllegalArgumentException ex) {
			return TEXT;
		}
	}

}
