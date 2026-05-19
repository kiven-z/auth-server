package com.auth.service.system.message.channel.dingtalk;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.ChannelOptionsSupport;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.dingtalk.DingTalkChannelOptions;
import com.auth.module.message.api.model.dingtalk.DingTalkMessageType;
import com.auth.module.message.sender.modules.dingtalk.RenderedDingTalkNotice;
import com.auth.module.message.sender.modules.dingtalk.client.DingTalkWorkNoticeClient;
import com.auth.module.message.sender.spi.MessageSender;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.*;

/**
 * 钉钉工作通知渠道发送实现（统一消息编排）
 *
 * @author Bunny
 */
@ConditionalOnProperty(prefix = "auth.message.ding-talk", name = "enabled", havingValue = "true")
@Component
public class DingTalkMessageSender implements MessageSender {

	private final DingTalkTemplatePipeline dingTalkTemplatePipeline;

	private final ObjectProvider<DingTalkWorkNoticeClient> workNoticeClient;

	public DingTalkMessageSender(DingTalkTemplatePipeline dingTalkTemplatePipeline,
			ObjectProvider<DingTalkWorkNoticeClient> workNoticeClient) {
		this.dingTalkTemplatePipeline = dingTalkTemplatePipeline;
		this.workNoticeClient = workNoticeClient;
	}

	/**
	 * 解析消息类型：options 覆盖模板结果
	 * @param rendered 模板渲染结果
	 * @param options 渠道扩展参数
	 * @return 消息类型
	 */
	private static DingTalkMessageType resolveMessageType(RenderedDingTalkNotice rendered,
			DingTalkChannelOptions options) {
		if (options != null && options.getMessageType() != null) {
			return options.getMessageType();
		}
		return rendered.messageType() != null ? rendered.messageType() : DingTalkMessageType.TEXT;
	}

	/**
	 * 解析 Markdown 标题：options 覆盖模板结果
	 * @param rendered 模板渲染结果
	 * @param options 渠道扩展参数
	 * @return 标题
	 */
	private static String resolveTitle(RenderedDingTalkNotice rendered, DingTalkChannelOptions options) {
		if (options != null && CharSequenceUtil.isNotBlank(options.getTitle())) {
			return options.getTitle();
		}
		return rendered.title();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageChannel channel() {
		return MessageChannel.DING_TALK;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendByTemplate(TemplateMessageCommand command) {
		List<String> targets = command.getTargets();
		if (CollUtil.isEmpty(targets)) {
			throw new SystemBusinessException(DING_TALK_TARGET_EMPTY);
		}

		DingTalkWorkNoticeClient client = workNoticeClient.getIfAvailable();
		if (client == null) {
			throw new SystemBusinessException(DING_TALK_NOT_ENABLED);
		}

		RenderedDingTalkNotice rendered = resolveContent(command);
		DingTalkChannelOptions options = ChannelOptionsSupport.asDingTalk(command.getOptions());
		DingTalkMessageType messageType = resolveMessageType(rendered, options);
		String title = resolveTitle(rendered, options);

		try {
			client.sendWorkNotice(targets, messageType, title, rendered.content());
		}
		catch (IllegalStateException | IllegalArgumentException ex) {
			throw new SystemBusinessException(DING_TALK_SEND_FAILED, ex.getMessage());
		}
	}

	/**
	 * 解析发送正文：自定义正文或模板渲染
	 * @param command 发送命令
	 * @return 渲染结果
	 */
	private RenderedDingTalkNotice resolveContent(TemplateMessageCommand command) {
		if (Boolean.TRUE.equals(command.getCustomBody())) {
			if (CharSequenceUtil.isBlank(command.getBody())) {
				throw new SystemBusinessException(DING_TALK_BODY_EMPTY);
			}
			DingTalkChannelOptions options = ChannelOptionsSupport.asDingTalk(command.getOptions());
			DingTalkMessageType type = options != null && options.getMessageType() != null ? options.getMessageType()
					: DingTalkMessageType.TEXT;
			String title = options != null ? options.getTitle() : null;
			return new RenderedDingTalkNotice(type, title, command.getBody());
		}

		Map<String, Object> variables = Optional.ofNullable(command.getVariables()).orElseGet(HashMap::new);
		return dingTalkTemplatePipeline.render(command.getTemplateCode(), variables);
	}

}
