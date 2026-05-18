package com.auth.module.message.sms4j;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.auth.module.message.config.properties.SmsProperties;
import com.auth.module.message.sms.SmsSender;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.factory.SmsFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 基于 sms4j 的模板短信发送实现
 *
 * @author Bunny
 */
@Slf4j
public class Sms4jSmsSender implements SmsSender {

	private final SmsProperties smsProperties;

	public Sms4jSmsSender(SmsProperties smsProperties) {
		this.smsProperties = smsProperties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendTemplateSms(String phone, String providerTemplateCode, String signName, String templateParamJson,
			String smsConfigId) {
		String configId = resolveConfigId(smsConfigId);
		SmsBlend smsBlend = requireSmsBlend(configId);
		LinkedHashMap<String, String> messages = parseTemplateParam(templateParamJson);

		SmsResponse response = invokeSend(smsBlend, phone, providerTemplateCode, messages, configId);
		assertSendSuccess(response, configId, phone, providerTemplateCode);
		log.debug("SMS4J send ok, configId={}, phone={}, templateId={}, signName={}", configId, phone,
				providerTemplateCode, signName);
	}

	/**
	 * 按 configId 获取已注册的 sms4j blend
	 * @param configId blend 标识
	 * @return 短信混合实例
	 */
	private SmsBlend requireSmsBlend(String configId) {
		SmsBlend smsBlend = SmsFactory.getSmsBlend(configId);
		if (smsBlend == null) {
			throw new IllegalStateException("SMS4J blend not found for configId: " + configId);
		}
		return smsBlend;
	}

	/**
	 * 调用 sms4j 发送接口，仅包装底层调用异常
	 * @param smsBlend 短信混合实例
	 * @param phone 手机号
	 * @param providerTemplateCode 模板代码
	 * @param messages 模板参数
	 * @param configId 配置ID
	 * @return 发送响应
	 */
	private SmsResponse invokeSend(SmsBlend smsBlend, String phone, String providerTemplateCode,
			LinkedHashMap<String, String> messages, String configId) {
		try {
			return smsBlend.sendMessage(phone, providerTemplateCode, messages);
		}
		catch (Exception e) {
			log.error("SMS4J send error, configId={}, phone={}, templateId={}", configId, phone, providerTemplateCode,
					e);
			throw new IllegalStateException("SMS4J send error: " + e.getMessage(), e);
		}
	}

	/**
	 * 校验厂商回执，失败时记录日志并抛出业务异常
	 * @param response 发送响应
	 * @param configId 配置ID
	 * @param phone 手机号
	 * @param providerTemplateCode 模板代码
	 */
	private void assertSendSuccess(SmsResponse response, String configId, String phone, String providerTemplateCode) {
		if (response != null && response.isSuccess()) {
			return;
		}
		String detail = resolveFailureDetail(response);
		log.error("SMS4J send failed, configId={}, phone={}, templateId={}, detail={}", configId, phone,
				providerTemplateCode, detail);
		throw new IllegalStateException("SMS4J send failed: " + detail);
	}

	/**
	 * 解析失败详情
	 * @param response 发送响应
	 * @return 失败详情
	 */
	private String resolveFailureDetail(SmsResponse response) {
		if (response == null) {
			return "null response";
		}
		if (response.getData() == null) {
			return "unknown";
		}
		return String.valueOf(response.getData());
	}

	/**
	 * 解析配置ID
	 * @param smsConfigId 短信配置ID
	 * @return 配置ID
	 */
	private String resolveConfigId(String smsConfigId) {
		if (CharSequenceUtil.isNotBlank(smsConfigId)) {
			return smsConfigId;
		}
		return smsProperties.getDefaultConfigId();
	}

	/**
	 * 解析模板参数
	 * @param templateParamJson 模板参数JSON
	 * @return 模板参数
	 */
	private LinkedHashMap<String, String> parseTemplateParam(String templateParamJson) {
		// 构建模板参数
		LinkedHashMap<String, String> messages = new LinkedHashMap<>();
		if (CharSequenceUtil.isBlank(templateParamJson)) {
			return messages;
		}

		// 解析模板参数
		JSONObject json = JSONUtil.parseObj(templateParamJson);
		for (Map.Entry<String, Object> entry : json.entrySet()) {
			Object value = entry.getValue();
			messages.put(entry.getKey(), value == null ? "" : String.valueOf(value));
		}
		return messages;
	}

}
