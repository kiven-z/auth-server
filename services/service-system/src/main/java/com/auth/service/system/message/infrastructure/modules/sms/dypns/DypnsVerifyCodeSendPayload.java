package com.auth.service.system.message.infrastructure.modules.sms.dypns;

/**
 * Prepared parameters for Aliyun Dypns {@code SendSmsVerifyCode}.
 *
 * @param providerTemplateCode vendor-approved template code
 * @param templateParam template variables JSON object string
 * @author Bunny
 */
public record DypnsVerifyCodeSendPayload(String providerTemplateCode, String templateParam) {

}
