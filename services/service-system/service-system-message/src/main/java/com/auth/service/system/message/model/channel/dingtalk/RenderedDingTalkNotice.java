package com.auth.service.system.message.model.channel.dingtalk;

import com.auth.module.message.api.model.dingtalk.DingTalkMessageType;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 渲染后的钉钉工作通知负载
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class RenderedDingTalkNotice {

	/**
	 * 消息类型
	 */
	DingTalkMessageType messageType;

	/**
	 * Markdown 标题（可选用于TEXT）
	 */
	String title;

	/**
	 * 正文内容
	 */
	String content;

}
