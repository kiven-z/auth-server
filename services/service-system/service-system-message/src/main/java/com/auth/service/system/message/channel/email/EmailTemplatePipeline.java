package com.auth.service.system.message.channel.email;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.exception.TemplateRenderException;
import com.auth.service.system.message.mapper.MessageTemplateMapper;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.support.template.FreemarkerTemplateRenderer;
import com.auth.service.system.message.support.template.MessageTemplateRequireFieldsJsonSupport;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_FAILED;
import static com.auth.service.system.message.exception.MessageResultCode.TEMPLATE_NOT_FOUND;
import static com.auth.service.system.message.exception.MessageResultCode.TEMPLATE_VARS_MISSING;

/**
 * 邮件模板加载、变量校验与渲染
 *
 * @author Bunny
 */
@Component
public class EmailTemplatePipeline {

	private final MessageTemplateMapper messageTemplateMapper;

	public EmailTemplatePipeline(MessageTemplateMapper messageTemplateMapper) {
		this.messageTemplateMapper = messageTemplateMapper;
	}

	/**
	 * 按模板编码渲染邮件主题与正文
	 * @param templateCode 模板编码
	 * @param variables 模板变量
	 * @return 渲染结果
	 */
	public RenderedEmail render(String templateCode, Map<String, Object> variables) {
		MessageTemplateEntity template = messageTemplateMapper.selectEnabledBySceneCodeAndChannel(templateCode,
				MessageChannel.EMAIL.name());
		if (template == null) {
			throw new SystemBusinessException(TEMPLATE_NOT_FOUND, MessageChannel.EMAIL.name(), templateCode);
		}
		Map<String, Object> safeVariables = Optional.ofNullable(variables).orElseGet(HashMap::new);
		MessageTemplateRequireFieldsJsonSupport.assertVariablesPresent(template.getRequireFields(), safeVariables,
				TEMPLATE_VARS_MISSING, MessageChannel.EMAIL.name());
		String subject = renderSubject(template, safeVariables);
		String body = renderContent(template, safeVariables);
		return new RenderedEmail(subject, body);
	}

	private String renderSubject(MessageTemplateEntity template, Map<String, Object> variables) {
		try {
			return FreemarkerTemplateRenderer.processTemplate(template.getTemplateName() + "_subject",
					template.getSubject(), variables);
		}
		catch (TemplateRenderException e) {
			throw new SystemBusinessException(OPERATION_FAILED, "subject", e.getMessage());
		}
	}

	private String renderContent(MessageTemplateEntity template, Map<String, Object> variables) {
		try {
			return FreemarkerTemplateRenderer.processTemplate(template.getTemplateName(), template.getBodyContent(),
					variables);
		}
		catch (TemplateRenderException e) {
			throw new SystemBusinessException(OPERATION_FAILED, "body", e.getMessage());
		}
	}

}
