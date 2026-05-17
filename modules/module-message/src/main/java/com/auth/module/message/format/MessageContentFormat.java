package com.auth.module.message.format;

/**
 * 消息正文内容形态（与具体传输协议解耦，供模板元数据与渲染策略选择）
 *
 * @author Bunny
 */
public enum MessageContentFormat {

	/**
	 * 纯文本
	 */
	PLAIN,

	/**
	 * HTML
	 */
	HTML,

	/**
	 * Markdown
	 */
	MARKDOWN

}
