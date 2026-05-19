package com.auth.service.system.message.channel.dingtalk;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.model.dingtalk.DingTalkMessageType;
import com.auth.service.system.admin.mapper.MessageTemplateMapper;
import com.auth.service.system.admin.model.entity.MessageTemplateEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.infrastructure.config.properties.DingTalkProperties;
import com.auth.service.system.message.infrastructure.modules.dingtalk.RenderedDingTalkNotice;
import com.auth.service.system.message.infrastructure.template.FreemarkerTemplateRenderer;
import com.auth.service.system.message.infrastructure.template.TemplateRenderException;
import com.auth.service.system.message.shared.MessageTemplateRequireFieldsJsonSupport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.*;

/**
 * 钉钉模板加载、变量校验与 FreeMarker 渲染
 *
 * @author Bunny
 */
@ConditionalOnProperty(prefix = "auth.message.ding-talk", name = "enabled", havingValue = "true")
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
			throw new SystemBusinessException(DING_TALK_TEMPLATE_NOT_FOUND, MessageChannel.DING_TALK.name(), sceneCode);
		}

		Map<String, Object> safeVariables = Optional.ofNullable(variables).orElseGet(HashMap::new);
		MessageTemplateRequireFieldsJsonSupport.assertVariablesPresent(template.getRequireFields(), safeVariables,
				DING_TALK_TEMPLATE_VARS_MISSING, MessageChannel.DING_TALK.name());

		DingTalkMessageType messageType = resolveMessageType(template);
		String title = renderOptionalField(template.getTemplateName() + "_title", template.getSubject(), safeVariables);
		String content = renderBody(template, safeVariables);
		return new RenderedDingTalkNotice(messageType, title, content);
	}

	/**
	 * 解析消息类型：模板 im_message_type 优先，否则配置默认
	 * @param template 模板实体
	 * @return 消息类型
	 */
	private DingTalkMessageType resolveMessageType(MessageTemplateEntity template) {
		if (CharSequenceUtil.isNotBlank(template.getImMessageType())) {
			try {
				return DingTalkMessageType.valueOf(template.getImMessageType().trim().toUpperCase());
			}
			catch (IllegalArgumentException ignored) {
				// 非法值回退默认
			}
		}
		return dingTalkProperties.getDefaultMessageType();
	}

	/**
	 * 渲染可选字段（主题/标题等）
	 * @param templateKey FreeMarker 模板名
	 * @param templateContent 模板内容
	 * @param variables 变量
	 * @return 渲染结果，模板为空时返回 null
	 */
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

	/**
	 * 渲染正文
	 * @param template 模板实体
	 * @param variables 变量
	 * @return 渲染后的正文
	 */
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
