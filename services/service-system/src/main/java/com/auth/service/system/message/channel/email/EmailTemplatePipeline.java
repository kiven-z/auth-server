package com.auth.service.system.message.channel.email;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.admin.mapper.EmailTemplateMapper;
import com.auth.service.system.admin.model.entity.EmailTemplateEntity;
import com.auth.service.system.admin.model.vo.emailtemplate.EmailTemplateRequireFieldRow;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.channel.email.model.RenderedEmail;
import com.auth.service.system.message.shared.EmailTemplateRequireFieldsJsonSupport;
import com.auth.service.system.message.shared.FreeMarkTemplateRenderer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.auth.service.system.common.exception.SystemResultCodeEnum.*;

/**
 * 邮件模板加载、变量校验与 FreeMarker 渲染
 *
 * @author Bunny
 */
@Component
public class EmailTemplatePipeline {

	private final EmailTemplateMapper emailTemplateMapper;

	public EmailTemplatePipeline(EmailTemplateMapper emailTemplateMapper) {
		this.emailTemplateMapper = emailTemplateMapper;
	}

	/**
	 * 按模板编码渲染邮件主题与正文
	 * @param templateCode 模板编码
	 * @param variables 模板变量
	 * @return 渲染结果
	 */
	public RenderedEmail render(String templateCode, Map<String, Object> variables) {
		EmailTemplateEntity template = emailTemplateMapper.selectTemplateByTemplateCode(templateCode);
		if (template == null) {
			throw new SystemBusinessException(EMAIL_TEMPLATE_NOT_FOUND, templateCode);
		}
		Map<String, Object> safeVariables = Optional.ofNullable(variables).orElseGet(HashMap::new);
		validateRequiredVariables(template, safeVariables);
		String subject = renderSubject(template, safeVariables);
		String body = renderContent(template, safeVariables);
		return new RenderedEmail(subject, body);
	}

	/**
	 * 校验必填变量
	 * @param template 邮件模板
	 * @param variables 模板变量
	 */
	private void validateRequiredVariables(EmailTemplateEntity template, Map<String, Object> variables) {
		// 校验必填（在页面中配置）
		List<EmailTemplateRequireFieldRow> requireFields = EmailTemplateRequireFieldsJsonSupport
			.parse(template.getRequireFields());
		if (CollUtil.isEmpty(requireFields)) {
			return;
		}

		List<String> missingKeys = requireFields.stream()
			.map(EmailTemplateRequireFieldRow::getKey)
			.filter(key -> !variables.containsKey(key))
			.toList();
		if (CollUtil.isNotEmpty(missingKeys)) {
			throw new SystemBusinessException(EMAIL_TEMPLATE_VARS_MISSING, String.join(", ", missingKeys));
		}
	}

	/**
	 * 渲染邮件主题
	 * @param template 邮件模板
	 * @param variables 模板变量
	 * @return 渲染后的主题
	 */
	private String renderSubject(EmailTemplateEntity template, Map<String, Object> variables) {
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
	private String renderContent(EmailTemplateEntity template, Map<String, Object> variables) {
		try {
			return FreeMarkTemplateRenderer.processTemplate(template.getTemplateName(), template.getContent(),
					variables);
		}
		catch (Exception e) {
			throw new SystemBusinessException(EMAIL_BODY_RENDER_FAILED, e.getMessage());
		}
	}

}
