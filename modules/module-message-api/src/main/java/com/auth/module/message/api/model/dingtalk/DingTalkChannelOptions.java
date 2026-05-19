package com.auth.module.message.api.model.dingtalk;

import com.auth.module.message.api.command.ChannelOptions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 钉钉渠道扩展参数
 *
 * @author Bunny
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DingTalkChannelOptions implements ChannelOptions {

    /**
     * Jackson 多态类型标识
     */
    public static final String TYPE = "ding_talk";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Markdown 标题（TEXT 类型可空）
     */
    private String title;

    /**
     * 覆盖模板或默认的消息类型
     */
    private DingTalkMessageType messageType;

}
