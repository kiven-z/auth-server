package com.auth.service.system.message.channel.dypns;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.config.properties.SmsProperties;
import com.auth.service.system.admin.mapper.MessageTemplateMapper;
import com.auth.service.system.admin.model.entity.MessageTemplateEntity;
import com.auth.service.system.admin.model.vo.emailtemplate.EmailTemplateRequireFieldRow;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.channel.dypns.model.DypnsVerifyCodePreparedMessage;
import com.auth.service.system.message.shared.MessageTemplateRequireFieldsJsonSupport;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.*;

/**
 * Dypns 验证码短信：模板加载、变量校验与 templateParam 组装（channel=SMS_DYPNS_VERIFY_CODE）
 *
 * @author Bunny
 */
@Component
public class DypnsVerifyCodeTemplatePipeline {

	private final MessageTemplateMapper messageTemplateMapper;

	private final SmsProperties smsProperties;

	public DypnsVerifyCodeTemplatePipeline(MessageTemplateMapper messageTemplateMapper, SmsProperties smsProperties) {
		this.messageTemplateMapper = messageTemplateMapper;
		this.smsProperties = smsProperties;
	}

	/**
	 * 构建模板参数
	 * @param requireFields 必填字段
	 * @param variables 变量
	 * @return 模板参数 JSON
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
	 * 准备 Dypns 验证码短信发送参数
	 * @param command 模板发送命令
	 * @return 厂商 API 就绪参数
	 */
	public DypnsVerifyCodePreparedMessage prepare(TemplateMessageCommand command) {
		String sceneCode = command.getTemplateCode();
		MessageTemplateEntity template = messageTemplateMapper.selectEnabledBySceneCodeAndChannel(sceneCode,
				MessageChannel.SMS_DYPNS_VERIFY_CODE.name());
		if (template == null) {
			throw new SystemBusinessException(SMS_TEMPLATE_NOT_FOUND, sceneCode);
		}

		Map<String, Object> variables = Optional.ofNullable(command.getVariables()).orElseGet(LinkedHashMap::new);
		List<EmailTemplateRequireFieldRow> requireFields = MessageTemplateRequireFieldsJsonSupport
			.parse(template.getRequireFields());
		MessageTemplateRequireFieldsJsonSupport.assertVariablesPresent(requireFields, variables,
				SMS_TEMPLATE_VARS_MISSING);

		String signName = resolveSignName();
		String templateParam = buildTemplateParam(requireFields, variables);

		return new DypnsVerifyCodePreparedMessage(template.getProviderTemplateCode(), signName, templateParam);
	}

	/**
	 * 解析 Dypns 短信签名
	 * @return 签名
	 */
	private String resolveSignName() {
		if (CharSequenceUtil.isNotBlank(smsProperties.getSignName())) {
			return smsProperties.getSignName();
		}
		throw new SystemBusinessException(SMS_SIGN_NAME_NOT_CONFIGURED);
	}

}
