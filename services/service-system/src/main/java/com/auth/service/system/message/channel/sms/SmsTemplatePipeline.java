package com.auth.service.system.message.channel.sms;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.config.properties.SmsProperties;
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

	public SmsTemplatePipeline(SmsTemplateMapper smsTemplateMapper, SmsProperties smsProperties) {
		this.smsTemplateMapper = smsTemplateMapper;
		this.smsProperties = smsProperties;
	}

	/**
	 * 准备短信发送参数
	 * @param command 模板发送命令
	 * @return 厂商 API 就绪参数
	 */
	public PreparedSms prepare(TemplateMessageCommand command) {
		SmsTemplateEntity template = smsTemplateMapper.selectByTemplateCode(command.getTemplateCode());
		if (template == null) {
			throw new SystemBusinessException(SMS_TEMPLATE_NOT_FOUND, command.getTemplateCode());
		}
		Map<String, Object> variables = Optional.ofNullable(command.getVariables()).orElseGet(LinkedHashMap::new);
		List<EmailTemplateRequireFieldRow> requireFields = MessageTemplateRequireFieldsJsonSupport
			.parse(template.getRequireFields());
		MessageTemplateRequireFieldsJsonSupport.assertVariablesPresent(requireFields, variables,
				SMS_TEMPLATE_VARS_MISSING);
		String templateParam = buildTemplateParam(requireFields, variables);
		String signName = resolveSignName(template);
		String countryCode = resolveCountryCode(command, template);
		String schemeName = resolveSchemeName(command);
		return new PreparedSms(template.getProviderTemplateCode(), signName, templateParam, countryCode, schemeName);
	}

	/**
	 * 构建模板参数
	 * @param requireFields 必填变量声明
	 * @param variables 变量
	 * @return 模板参数
	 */
	private String buildTemplateParam(List<EmailTemplateRequireFieldRow> requireFields, Map<String, Object> variables) {
		Map<String, String> paramMap = new LinkedHashMap<>();

		// 构建模板参数
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
	 * 解析签名名称
	 * @param template 模板
	 * @return 签名名称
	 */
	private String resolveSignName(SmsTemplateEntity template) {
		// 解析签名名称
		if (CharSequenceUtil.isNotBlank(template.getSignName())) {
			return template.getSignName();
		}
		// 解析阿里云签名名称
		if (smsProperties.getAliyun() != null
				&& CharSequenceUtil.isNotBlank(smsProperties.getAliyun().getDefaultSignName())) {
			return smsProperties.getAliyun().getDefaultSignName();
		}
		throw new SystemBusinessException(SMS_SIGN_NAME_NOT_CONFIGURED);
	}

	/**
	 * 解析国家代码
	 * @param command 命令
	 * @param template 模板
	 * @return 国家代码
	 */
	private String resolveCountryCode(TemplateMessageCommand command, SmsTemplateEntity template) {
		// 解析国家代码
		SmsChannelOptions options = command.getSmsOptions();
		if (options != null && CharSequenceUtil.isNotBlank(options.getCountryCode())) {
			return options.getCountryCode();
		}
		// 解析默认国家代码
		if (CharSequenceUtil.isNotBlank(template.getDefaultCountryCode())) {
			return template.getDefaultCountryCode();
		}
		return "86";
	}

	/**
	 * 解析方案名称
	 * @param command 命令
	 * @return 方案名称
	 */
	private String resolveSchemeName(TemplateMessageCommand command) {
		// 解析方案名称
		SmsChannelOptions options = command.getSmsOptions();
		if (options != null && CharSequenceUtil.isNotBlank(options.getSchemeName())) {
			return options.getSchemeName();
		}
		// 解析阿里云方案名称
		if (smsProperties.getAliyun() != null) {
			return smsProperties.getAliyun().getDefaultSchemeName();
		}
		return null;
	}

}
