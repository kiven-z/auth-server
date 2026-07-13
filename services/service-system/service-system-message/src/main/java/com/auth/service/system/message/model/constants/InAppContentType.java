package com.auth.service.system.message.model.constants;

import cn.hutool.core.text.CharSequenceUtil;

/**
 * 站内信正文类型（写入 message_template.im_message_type / 发送 content_type）
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
	MARKDOWN,

	/**
	 * 受限 HTML（须经白名单消毒）
	 */
	HTML;

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
