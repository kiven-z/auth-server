package com.auth.service.system.message.channel.dypns.model;

/**
 * Dypns 验证码短信发送就绪参数（SendSmsVerifyCode 入参）
 *
 * @author Bunny
 */
public record DypnsVerifyCodePreparedMessage(String providerTemplateCode, String signName, String templateParam) {

}
