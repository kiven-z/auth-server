package com.auth.service.system.message.channel.sms.model;

import com.auth.module.message.sms.SmsSendMode;

/**
 * 短信发送就绪上下文（厂商 API 入参）
 *
 * @author Bunny
 * @param sendMode 发送模式
 * @param providerTemplateCode 厂商模板 CODE
 * @param signName 短信签名
 * @param templateParam 模板变量 JSON
 * @param countryCode 国家码（VERIFY）
 * @param schemeName 阿里云方案名（VERIFY，可为空）
 * @param smsConfigId sms4j configId（NOTIFY，可为空则用默认）
 */
public record PreparedSms(SmsSendMode sendMode, String providerTemplateCode, String signName, String templateParam,
		String countryCode, String schemeName, String smsConfigId) {

}
