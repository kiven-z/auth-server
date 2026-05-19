package com.auth.module.message.modules.dingtalk.model;

import com.auth.module.message.modules.dingtalk.DingTalkMessageType;

/**
 * Rendered DingTalk work-notice payload.
 *
 * @param messageType message type
 * @param title Markdown title (optional for TEXT)
 * @param content body content
 * @author Bunny
 */
public record RenderedDingTalkNotice(DingTalkMessageType messageType, String title, String content) {

}
