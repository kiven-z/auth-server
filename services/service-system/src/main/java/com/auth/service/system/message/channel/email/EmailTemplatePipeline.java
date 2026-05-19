package com.auth.service.system.message.channel.email;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.service.system.admin.mapper.MessageTemplateMapper;
import com.auth.service.system.admin.model.entity.MessageTemplateEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.infrastructure.modules.email.RenderedEmail;
import com.auth.service.system.message.infrastructure.template.FreemarkerTemplateRenderer;
import com.auth.service.system.message.infrastructure.template.TemplateRenderException;
import com.auth.service.system.message.shared.MessageTemplateRequireFieldsJsonSupport;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.auth.service.system.message.infrastructure.exception.SystemEmailResultCode.*;

/**
 * 邮件模板加载、变量校验与 FreeMarker 渲染
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
			throw new SystemBusinessException(EMAIL_TEMPLATE_NOT_FOUND, MessageChannel.EMAIL.name(), templateCode);
		}
		Map<String, Object> safeVariables = Optional.ofNullable(variables).orElseGet(HashMap::new);
		MessageTemplateRequireFieldsJsonSupport.assertVariablesPresent(template.getRequireFields(), safeVariables,
				EMAIL_TEMPLATE_VARS_MISSING, MessageChannel.EMAIL.name());
		String subject = renderSubject(template, safeVariables);
		String body = renderContent(template, safeVariables);
		return new RenderedEmail(subject, body);
	}

	/**
	 * 渲染邮件主题
	 * @param template 邮件模板
	 * @param variables 模板变量
	 * @return 渲染后的主题
	 */
	private String renderSubject(MessageTemplateEntity template, Map<String, Object> variables) {
		try {
			return FreemarkerTemplateRenderer.processTemplate(template.getTemplateName() + "_subject",
					template.getSubject(), variables);
		}
		catch (TemplateRenderException e) {
			throw new SystemBusinessException(EMAIL_RENDER_FAILED, "subject", e.getMessage());
		}
	}

	/**
	 * 渲染邮件正文
	 * @param template 邮件模板
	 * @param variables 模板变量
	 * @return 渲染后的正文
	 */
	private String renderContent(MessageTemplateEntity template, Map<String, Object> variables) {
		try {
			return FreemarkerTemplateRenderer.processTemplate(template.getTemplateName(), template.getBodyContent(),
					variables);
		}
		catch (TemplateRenderException e) {
			throw new SystemBusinessException(EMAIL_RENDER_FAILED, "body", e.getMessage());
		}
	}

}
