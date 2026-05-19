package com.auth.module.message.api.model.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 邮件附件（跨服务契约）
 *
 * @author Bunny
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件名
     */
    private String filename;

    /**
     * MIME 类型
     */
    private String contentType;

    /**
     * Base64 编码内容
     */
    private String base64;

    /**
     * 原始字节
     */
    private byte[] bytes;

    /**
     * 远程下载地址
     */
    private String url;

}
