package com.auth.service.system.message.channel.dingtalk;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.model.dingtalk.DingTalkMessageType;
import com.auth.service.system.message.config.properties.DingTalkProperties;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.value.dingtalk.RenderedDingTalkNotice;
import com.auth.service.system.message.support.template.FreemarkerTemplateRenderer;
import com.auth.service.system.message.support.template.MessageTemplateLoader;
import com.auth.service.system.message.support.template.MessageTemplateRequireFieldsJsonSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.message.exception.MessageResultCode.TEMPLATE_VARS_MISSING;

/**
 * 钉钉模板加载、变量校验与渲染
 *
 * @author Bunny
 */
@Slf4j
@Component
public class DingTalkTemplatePipeline {

	private final MessageTemplateLoader messageTemplateLoader;

	private final DingTalkProperties dingTalkProperties;

	public DingTalkTemplatePipeline(MessageTemplateLoader messageTemplateLoader,
			DingTalkProperties dingTalkProperties) {
		this.messageTemplateLoader = messageTemplateLoader;
		this.dingTalkProperties = dingTalkProperties;
	}

	/**
	 * 按场景编码渲染钉钉工作通知正文
	 * @param sceneCode 场景编码（发送命令 templateCode）
	 * @param variables 模板变量
	 * @return 渲染结果
	 */
	public RenderedDingTalkNotice render(String sceneCode, Map<String, Object> variables) {
		MessageTemplateEntity template = messageTemplateLoader.loadEnabled(MessageChannel.DING_TALK, sceneCode);

		Map<String, Object> safeVariables = Optional.ofNullable(variables).orElseGet(HashMap::new);
		var requireFields = MessageTemplateRequireFieldsJsonSupport.parse(template.getRequireFields());
		MessageTemplateRequireFieldsJsonSupport.assertVariablesPresent(requireFields, safeVariables,
				TEMPLATE_VARS_MISSING, MessageChannel.DING_TALK.name());

		DingTalkMessageType messageType = resolveMessageType(template);
		String subject = template.getSubject();
		String templateName = template.getTemplateName();
		String bodyContent = template.getBodyContent();
		String title = CharSequenceUtil.isBlank(subject) ? null
				: FreemarkerTemplateRenderer.processTemplate(templateName + "_title", subject, safeVariables);

		if (CharSequenceUtil.isBlank(bodyContent)) {
			throw new MessageException(PARAM_REQUIRED, "钉钉消息正文");
		}

		String content = FreemarkerTemplateRenderer.processTemplate(templateName, bodyContent, safeVariables);
		return RenderedDingTalkNotice.builder().messageType(messageType).title(title).content(content).build();
	}

	/**
	 * 模板上的消息类型优先，无效或缺失时用渠道默认
	 * @param template 模板
	 * @return 消息类型
	 */
	private DingTalkMessageType resolveMessageType(MessageTemplateEntity template) {
		String type = template.getImMessageType();
		if (CharSequenceUtil.isBlank(type)) {
			return dingTalkProperties.getDefaultMessageType();
		}

		try {
			return DingTalkMessageType.valueOf(type.trim().toUpperCase());
		}
		catch (IllegalArgumentException ex) {
			log.warn("Invalid DingTalk message type [{}] on template [{}], using channel default.",
					template.getImMessageType(), template.getTemplateName(), ex);
			return dingTalkProperties.getDefaultMessageType();
		}
	}

}