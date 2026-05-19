package com.auth.module.message.sender.modules.sms;

/**
 * Prepared parameters for Aliyun Dypns {@code SendSmsVerifyCode}.
 *
 * @param providerTemplateCode vendor-approved template code
 * @param templateParam        template variables JSON object string
 * @author Bunny
 */
public record DypnsVerifyCodeSendPayload(String providerTemplateCode, String templateParam) {

}
