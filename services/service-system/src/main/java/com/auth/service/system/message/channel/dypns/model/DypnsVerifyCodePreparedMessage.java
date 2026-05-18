package com.auth.service.system.message.channel.dypns.model;

/**
 * Dypns 验证码短信发送就绪参数（SendSmsVerifyCode 入参；签名由全局配置提供）
 *
 * @author Bunny
 */
public record DypnsVerifyCodePreparedMessage(String providerTemplateCode, String templateParam) {

}
