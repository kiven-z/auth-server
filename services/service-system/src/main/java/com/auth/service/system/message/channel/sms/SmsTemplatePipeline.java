package com.auth.service.system.message.channel.sms;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.auth.module.message.command.ChannelOptionsSupport;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.config.properties.SmsProperties;
import com.auth.module.message.sms.SmsChannelOptions;
import com.auth.service.system.admin.mapper.SmsTemplateMapper;
import com.auth.service.system.admin.model.entity.SmsTemplateEntity;
import com.auth.service.system.admin.model.vo.emailtemplate.EmailTemplateRequireFieldRow;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.channel.sms.model.SmsPreparedMessage;
import com.auth.service.system.message.shared.MessageTemplateRequireFieldsJsonSupport;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.*;

/**
 * 短信模板加载、变量校验与 templateParam 组装
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
	 * 构建模板参数
	 * @param requireFields 必填字段
	 * @param variables 变量
	 * @return 模板参数
	 */
	private static String buildTemplateParam(List<EmailTemplateRequireFieldRow> requireFields,
			Map<String, Object> variables) {
		Map<String, String> paramMap = new LinkedHashMap<>();
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
	 * 准备短信发送参数
	 * @param command 模板发送命令
	 * @return 厂商 API 就绪参数
	 */
	public SmsPreparedMessage prepare(TemplateMessageCommand command) {
		// 获取模板代码
		String templateCode = command.getTemplateCode();
		SmsTemplateEntity template = smsTemplateMapper.selectByTemplateCode(templateCode);
		if (template == null) {
			throw new SystemBusinessException(SMS_TEMPLATE_NOT_FOUND, templateCode);
		}

		// 获取变量
		Map<String, Object> variables = Optional.ofNullable(command.getVariables()).orElseGet(LinkedHashMap::new);
		// 获取必填字段
		List<EmailTemplateRequireFieldRow> requireFields = MessageTemplateRequireFieldsJsonSupport
			.parse(template.getRequireFields());
		MessageTemplateRequireFieldsJsonSupport.assertVariablesPresent(requireFields, variables,
				SMS_TEMPLATE_VARS_MISSING);

		String smsConfigId = resolveSmsConfigId(template, command);
		String signName = resolveSignName();
		String templateParam = buildTemplateParam(requireFields, variables);

		return new SmsPreparedMessage(template.getProviderTemplateCode(), signName, templateParam, smsConfigId);
	}

	/**
	 * 解析短信配置ID
	 * @param template 短信模板
	 * @param command 模板发送命令
	 * @return 短信配置ID
	 */
	private String resolveSmsConfigId(SmsTemplateEntity template, TemplateMessageCommand command) {
		SmsChannelOptions options = ChannelOptionsSupport.asSms(command.getOptions());
		if (options != null && CharSequenceUtil.isNotBlank(options.getSmsConfigId())) {
			return options.getSmsConfigId();
		}

		String smsConfigId = template.getSmsConfigId();
		if (CharSequenceUtil.isNotBlank(smsConfigId)) {
			return smsConfigId;
		}

		return smsProperties.getDefaultConfigId();
	}

	/**
	 * 解析签名
	 * @return 签名
	 */
	private String resolveSignName() {
		if (CharSequenceUtil.isNotBlank(smsProperties.getDefaultSignName())) {
			return smsProperties.getDefaultSignName();
		}
		throw new SystemBusinessException(SMS_SIGN_NAME_NOT_CONFIGURED);
	}

}
