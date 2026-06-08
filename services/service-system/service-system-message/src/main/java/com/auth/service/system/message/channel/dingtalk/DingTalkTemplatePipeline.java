package com.auth.service.system.message.channel.dingtalk;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.model.dingtalk.DingTalkMessageType;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.exception.TemplateRenderException;
import com.auth.service.system.message.config.DingTalkProperties;
import com.auth.service.system.message.mapper.MessageTemplateMapper;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.template.FreemarkerTemplateRenderer;
import com.auth.service.system.message.template.MessageTemplateRequireFieldsJsonSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.auth.service.system.message.exception.MessageResultCode.*;

/**
 * 钉钉模板加载、变量校验与渲染
 *
 * @author Bunny
 */
@Slf4j
@Component
public class DingTalkTemplatePipeline {

	private final MessageTemplateMapper messageTemplateMapper;

	private final DingTalkProperties dingTalkProperties;

	public DingTalkTemplatePipeline(MessageTemplateMapper messageTemplateMapper,
			DingTalkProperties dingTalkProperties) {
		this.messageTemplateMapper = messageTemplateMapper;
		this.dingTalkProperties = dingTalkProperties;
	}

	/**
	 * 按场景编码渲染钉钉工作通知正文
	 * @param sceneCode 场景编码（发送命令 templateCode）
	 * @param variables 模板变量
	 * @return 渲染结果
	 */
	public RenderedDingTalkNotice render(String sceneCode, Map<String, Object> variables) {
		MessageTemplateEntity template = messageTemplateMapper.selectEnabledBySceneCodeAndChannel(sceneCode,
				MessageChannel.DING_TALK.name());
		if (template == null) {
			throw new SystemBusinessException(TEMPLATE_NOT_FOUND, MessageChannel.DING_TALK.name(), sceneCode);
		}

		Map<String, Object> safeVariables = Optional.ofNullable(variables).orElseGet(HashMap::new);
		MessageTemplateRequireFieldsJsonSupport.assertVariablesPresent(template.getRequireFields(), safeVariables,
				TEMPLATE_VARS_MISSING, MessageChannel.DING_TALK.name());

		DingTalkMessageType messageType = resolveMessageType(template);
		String title = renderOptionalField(template.getTemplateName() + "_title", template.getSubject(), safeVariables);
		String content = renderBody(template, safeVariables);
		return new RenderedDingTalkNotice(messageType, title, content);
	}

	/**
	 * 模板上的消息类型优先，无效或缺失时用渠道默认
	 */
	private DingTalkMessageType resolveMessageType(MessageTemplateEntity template) {
		if (CharSequenceUtil.isNotBlank(template.getImMessageType())) {
			try {
				return DingTalkMessageType.valueOf(template.getImMessageType().trim().toUpperCase());
			}
			catch (IllegalArgumentException ex) {
				log.warn("Invalid DingTalk message type [{}] on template [{}], using channel default.",
						template.getImMessageType(), template.getTemplateName(), ex);
			}
		}
		return dingTalkProperties.getDefaultMessageType();
	}

	private String renderOptionalField(String templateKey, String templateContent, Map<String, Object> variables) {
		if (CharSequenceUtil.isBlank(templateContent)) {
			return null;
		}
		try {
			return FreemarkerTemplateRenderer.processTemplate(templateKey, templateContent, variables);
		}
		catch (TemplateRenderException e) {
			throw new SystemBusinessException(DING_TALK_RENDER_FAILED, e.getMessage());
		}
	}

	private String renderBody(MessageTemplateEntity template, Map<String, Object> variables) {
		if (CharSequenceUtil.isBlank(template.getBodyContent())) {
			throw new SystemBusinessException(DING_TALK_BODY_EMPTY);
		}
		try {
			return FreemarkerTemplateRenderer.processTemplate(template.getTemplateName(), template.getBodyContent(),
					variables);
		}
		catch (TemplateRenderException e) {
			throw new SystemBusinessException(DING_TALK_RENDER_FAILED, e.getMessage());
		}
	}

}
