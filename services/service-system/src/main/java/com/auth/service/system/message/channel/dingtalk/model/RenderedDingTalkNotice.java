package com.auth.service.system.message.channel.dingtalk.model;

import com.auth.module.message.modules.dingtalk.DingTalkMessageType;

/**
 * 钉钉工作通知渲染结果
 *
 * @param messageType 消息类型
 * @param title Markdown 标题
 * @param content 正文
 * @author Bunny
 */
public record RenderedDingTalkNotice(DingTalkMessageType messageType, String title, String content) {

}
