package com.auth.service.system.message.channel.sms.model;

/**
 * 短信发送就绪参数（厂商 API 入参）
 *
 * @author Bunny
 */
public record SmsPreparedMessage(String providerTemplateCode, String signName, String templateParam) {

}
