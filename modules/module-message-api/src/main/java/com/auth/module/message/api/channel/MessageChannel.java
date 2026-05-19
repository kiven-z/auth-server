package com.auth.module.message.api.channel;

/**
 * 消息发送渠道
 *
 * @author Bunny
 */
public enum MessageChannel {

    /**
     * 电子邮件
     */
    EMAIL,

    /**
     * 阿里云 Dypns 验证码短信（SendSmsVerifyCode；与通用 dysmsapi 模板短信 SMS 不同）
     */
    SMS_DYPNS_VERIFY_CODE,

    /**
     * 钉钉
     */
    DING_TALK,

    /**
     * 站内信
     */
    IN_APP,
}
