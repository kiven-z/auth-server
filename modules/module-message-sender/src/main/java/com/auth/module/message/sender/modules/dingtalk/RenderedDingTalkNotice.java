package com.auth.module.message.sender.modules.dingtalk;


import com.auth.module.message.api.model.dingtalk.DingTalkMessageType;

/**
 * Rendered DingTalk work-notice payload.
 *
 * @param messageType message type
 * @param title       Markdown title (optional for TEXT)
 * @param content     body content
 * @author Bunny
 */
public record RenderedDingTalkNotice(DingTalkMessageType messageType, String title, String content) {

}
