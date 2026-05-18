package com.auth.service.system.message.channel.sms;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.auth.module.message.command.ChannelOptionsSupport;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.config.properties.SmsNotificationBlendProperties;
import com.auth.module.message.config.properties.SmsProperties;
import com.auth.module.message.sms.SmsSendMode;
import com.auth.module.message.sms.aliyun.AliyunSmsProperties;
import com.auth.module.message.sms.aliyun.SmsChannelOptions;
import com.auth.service.system.admin.mapper.SmsTemplateMapper;
import com.auth.service.system.admin.model.entity.SmsTemplateEntity;
import com.auth.service.system.admin.model.vo.emailtemplate.EmailTemplateRequireFieldRow;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.channel.sms.model.PreparedSms;
import com.auth.service.system.message.shared.MessageTemplateRequireFieldsJsonSupport;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.*;

/**
 * 短信模板加载、变量校验与 templateParam JSON 组装（不渲染正文）
 *
 * @author Bunny
 */
@Component
public class SmsTemplatePipeline {

	private final SmsTemplateMapper smsTemplateMapper;

	private final SmsProperties smsProperties;

	private final AliyunSmsProperties aliyunSmsProperties;

	public SmsTemplatePipeline(SmsTemplateMapper smsTemplateMapper, SmsProperties smsProperties,
			AliyunSmsProperties aliyunSmsProperties) {
		this.smsTemplateMapper = smsTemplateMapper;
		this.smsProperties = smsProperties;
		this.aliyunSmsProperties = aliyunSmsProperties;
	}

	/**
	 * 准备短信发送参数
	 * @param command 模板发送命令
	 * @return 厂商 API 就绪参数
	 */
	public PreparedSms prepare(TemplateMessageCommand command) {
		// 查询短信模板
		SmsTemplateEntity template = smsTemplateMapper.selectByTemplateCode(command.getTemplateCode());
		if (template == null) {
			throw new SystemBusinessException(SMS_TEMPLATE_NOT_FOUND, command.getTemplateCode());
		}
		// 解析发送模式
		SmsSendMode sendMode = resolveSendMode(template);
		// 解析短信配置ID
		String smsConfigId = resolveSmsConfigId(template);
		// 解析变量
		Map<String, Object> variables = Optional.ofNullable(command.getVariables()).orElseGet(LinkedHashMap::new);
		// 解析必填字段
		List<EmailTemplateRequireFieldRow> requireFields = MessageTemplateRequireFieldsJsonSupport
			.parse(template.getRequireFields());
			
		// 校验变量是否存在
		MessageTemplateRequireFieldsJsonSupport.assertVariablesPresent(requireFields, variables,
				SMS_TEMPLATE_VARS_MISSING);
		// 构建模板参数
		String templateParam = buildTemplateParam(requireFields, variables);
		// 解析签名
		String signName = resolveSignName(template, sendMode, smsConfigId);
		// 解析国家码
		String countryCode = resolveCountryCode(command, template);
		// 解析方案名
		String schemeName = resolveSchemeName(command);

		// 返回准备好的短信发送参数
		return new PreparedSms(sendMode, template.getProviderTemplateCode(), signName, templateParam, countryCode,
				schemeName, smsConfigId);
	}

	/**
	 * 解析发送模式
	 * @param template 短信模板
	 * @return 发送模式
	 */
	private SmsSendMode resolveSendMode(SmsTemplateEntity template) {
		if (CharSequenceUtil.isBlank(template.getTemplateType())) {
			return SmsSendMode.VERIFY;
		}
		try {
			return SmsSendMode.valueOf(template.getTemplateType().trim().toUpperCase());
		}
		catch (IllegalArgumentException ex) {
			throw new SystemBusinessException(SMS_TEMPLATE_TYPE_INVALID, template.getTemplateType());
		}
	}

/**
 * 解析短信配置ID
 * @param template 短信模板
 * @return 短信配置ID
 */
	private String resolveSmsConfigId(SmsTemplateEntity template) {
		if (CharSequenceUtil.isNotBlank(template.getSmsConfigId())) {
			return template.getSmsConfigId();
		}
		if (smsProperties.getNotification() != null) {
			return smsProperties.getNotification().getDefaultConfigId();
		}
		return null;
	}

/**
 * 构建模板参数
 * @param requireFields 必填字段
 * @param variables 变量
 * @return 模板参数
 */
	private String buildTemplateParam(List<EmailTemplateRequireFieldRow> requireFields, Map<String, Object> variables) {
		// 构建模板参数
		Map<String, String> paramMap = new LinkedHashMap<>();
		// 遍历必填字段
		if (CollUtil.isNotEmpty(requireFields)) {
			for (EmailTemplateRequireFieldRow field : requireFields) {
				Object value = variables.get(field.getKey());
				paramMap.put(field.getKey(), value == null ? "" : String.valueOf(value));
			}
		}
		else {
			variables.forEach((key, value) -> paramMap.put(key, value == null ? "" : String.valueOf(value)));
		}
		return JSONUtil.toJsonStr(paramMap);
	}

/**
 * 解析签名
 * @param template 短信模板
 * @param sendMode 发送模式
 * @param smsConfigId 短信配置ID
 * @return 签名
 */
	private String resolveSignName(SmsTemplateEntity template, SmsSendMode sendMode, String smsConfigId) {
		if (CharSequenceUtil.isNotBlank(template.getSignName())) {
			return template.getSignName();
		}
		if (sendMode == SmsSendMode.NOTIFY) {
			return resolveNotificationSignature(smsConfigId);
		}
		if (aliyunSmsProperties != null && CharSequenceUtil.isNotBlank(aliyunSmsProperties.getDefaultSignName())) {
			return aliyunSmsProperties.getDefaultSignName();
		}
		throw new SystemBusinessException(SMS_SIGN_NAME_NOT_CONFIGURED);
	}

/**
 * 解析通知签名
 * @param smsConfigId 短信配置ID
 * @return 通知签名
 */
	private String resolveNotificationSignature(String smsConfigId) {
		String configId = CharSequenceUtil.blankToDefault(smsConfigId,
				smsProperties.getNotification().getDefaultConfigId());
		SmsNotificationBlendProperties blend = smsProperties.getNotification().getBlends().get(configId);
		if (blend != null && CharSequenceUtil.isNotBlank(blend.getSignature())) {
			return blend.getSignature();
		}
		throw new SystemBusinessException(SMS_SIGN_NAME_NOT_CONFIGURED);
	}

/**
 * 解析国家码
 * @param command 模板发送命令
 * @param template 短信模板
 * @return 国家码
 */
	private String resolveCountryCode(TemplateMessageCommand command, SmsTemplateEntity template) {
		SmsChannelOptions options = ChannelOptionsSupport.asSms(command.getOptions());
		if (options != null && CharSequenceUtil.isNotBlank(options.getCountryCode())) {
			return options.getCountryCode();
		}
		if (CharSequenceUtil.isNotBlank(template.getDefaultCountryCode())) {
			return template.getDefaultCountryCode();
		}
		return "86";
	}

/**
 * 解析方案名
 * @param command 模板发送命令
 * @return 方案名
 */
	private String resolveSchemeName(TemplateMessageCommand command) {
		SmsChannelOptions options = ChannelOptionsSupport.asSms(command.getOptions());
		if (options != null && CharSequenceUtil.isNotBlank(options.getSchemeName())) {
			return options.getSchemeName();
		}
		if (aliyunSmsProperties != null) {
			return aliyunSmsProperties.getDefaultSchemeName();
		}
		return null;
	}

}
