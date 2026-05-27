package com.auth.service.system.message.channel.sms.dypns;

/**
 * Dypns 验证码短信发送就绪参数
 *
 * @param providerTemplateCode 厂商侧模板编码
 * @param templateParam 模板变量 JSON
 * @author Bunny
 */
public record DypnsVerifyCodeSendPayload(String providerTemplateCode, String templateParam) {

}
