package com.auth.service.system.message.channel.sms.model;

/**
 * 短信发送就绪上下文（厂商 API 入参）
 *
 * @author Bunny
 * @param providerTemplateCode 厂商模板 CODE
 * @param signName 短信签名
 * @param templateParam 模板变量 JSON
 * @param countryCode 国家码
 * @param schemeName 阿里云方案名（可为空）
 */
public record PreparedSms(String providerTemplateCode, String signName, String templateParam, String countryCode,
		String schemeName) {

}
