package com.auth.service.system.message.channel.sms.dypns;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.mapper.MessageTemplateMapper;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.vo.EmailTemplateRequireFieldRow;
import com.auth.service.system.message.template.MessageTemplateRequireFieldsJsonSupport;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.auth.service.system.message.exception.MessageResultCode.SMS_TEMPLATE_NOT_FOUND;
import static com.auth.service.system.message.exception.MessageResultCode.SMS_TEMPLATE_VARS_MISSING;

/**
 * Dypns 验证码短信：模板加载、变量校验与发送参数组装
 *
 * @author Bunny
 */
@Component
public class DypnsVerifyCodeTemplatePipeline {

	private final MessageTemplateMapper messageTemplateMapper;

	public DypnsVerifyCodeTemplatePipeline(MessageTemplateMapper messageTemplateMapper) {
		this.messageTemplateMapper = messageTemplateMapper;
	}

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

	public DypnsVerifyCodeSendPayload prepare(TemplateMessageCommand command) {
		String sceneCode = command.getTemplateCode();

		MessageTemplateEntity template = messageTemplateMapper.selectEnabledBySceneCodeAndChannel(sceneCode,
				MessageChannel.SMS_ALIYUN_DYPNS.name());
		if (template == null) {
			throw new SystemBusinessException(SMS_TEMPLATE_NOT_FOUND, MessageChannel.SMS_ALIYUN_DYPNS.name(),
					sceneCode);
		}

		Map<String, Object> variables = Optional.ofNullable(command.getVariables()).orElseGet(LinkedHashMap::new);
		List<EmailTemplateRequireFieldRow> requireFields = MessageTemplateRequireFieldsJsonSupport
			.parse(template.getRequireFields());
		MessageTemplateRequireFieldsJsonSupport.assertVariablesPresent(requireFields, variables,
				SMS_TEMPLATE_VARS_MISSING, MessageChannel.SMS_ALIYUN_DYPNS.name());

		String templateParam = buildTemplateParam(requireFields, variables);
		return new DypnsVerifyCodeSendPayload(template.getProviderTemplateCode(), templateParam);
	}

}
