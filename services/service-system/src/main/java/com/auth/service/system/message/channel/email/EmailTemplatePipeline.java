package com.auth.service.system.message.channel.email;

import com.auth.module.message.channel.MessageChannel;
import com.auth.service.system.admin.mapper.MessageTemplateMapper;
import com.auth.service.system.admin.model.entity.MessageTemplateEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.channel.email.model.RenderedEmail;
import com.auth.service.system.message.shared.MessageTemplateRequireFieldsJsonSupport;
import com.auth.service.system.message.shared.FreeMarkTemplateRenderer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.auth.service.system.common.exception.code.message.SystemEmailResultCode.*;

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
			throw new SystemBusinessException(EMAIL_TEMPLATE_NOT_FOUND, templateCode);
		}
		Map<String, Object> safeVariables = Optional.ofNullable(variables).orElseGet(HashMap::new);
		MessageTemplateRequireFieldsJsonSupport.assertVariablesPresent(template.getRequireFields(), safeVariables,
				EMAIL_TEMPLATE_VARS_MISSING);
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
			return FreeMarkTemplateRenderer.processTemplate(template.getTemplateName() + "_subject",
					template.getSubject(), variables);
		}
		catch (Exception e) {
			throw new SystemBusinessException(EMAIL_SUBJECT_RENDER_FAILED, e.getMessage());
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
			return FreeMarkTemplateRenderer.processTemplate(template.getTemplateName(), template.getBodyContent(),
					variables);
		}
		catch (Exception e) {
			throw new SystemBusinessException(EMAIL_BODY_RENDER_FAILED, e.getMessage());
		}
	}

}
