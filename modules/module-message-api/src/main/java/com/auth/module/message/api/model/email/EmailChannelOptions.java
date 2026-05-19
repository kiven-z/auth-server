package com.auth.module.message.api.model.email;

import com.auth.module.message.api.command.ChannelOptions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.List;

/**
 * 邮件渠道扩展参数
 *
 * @author Bunny
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailChannelOptions implements ChannelOptions {

    /**
     * Jackson 多态类型标识
     */
    public static final String TYPE = "email";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 抄送
     */
    private List<String> cc;

    /**
     * 密送
     */
    private List<String> bcc;

    /**
     * 回复地址
     */
    private String replyTo;

    /**
     * 正文是否为 HTML
     */
    private Boolean hasHtml;

    /**
     * 附件列表
     */
    private List<AttachmentDTO> attachments;

}
