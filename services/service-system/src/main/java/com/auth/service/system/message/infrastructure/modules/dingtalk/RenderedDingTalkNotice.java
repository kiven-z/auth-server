package com.auth.service.system.message.infrastructure.modules.dingtalk;

import com.auth.module.message.api.model.dingtalk.DingTalkMessageType;

/**
 * 渲染后的钉钉工作通知负载
 *
 * @param messageType 消息类型
 * @param title Markdown 标题（可选用于TEXT）
 * @param content 正文内容
 * @author Bunny
 */
public record RenderedDingTalkNotice(DingTalkMessageType messageType, String title, String content) {

}
