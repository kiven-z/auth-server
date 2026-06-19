package com.auth.service.system.message.channel.dingtalk;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.channel.MessageSender;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.dingtalk.DingTalkChannelOptions;
import com.auth.module.message.api.model.dingtalk.DingTalkMessageType;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.channel.dingtalk.client.DingTalkWorkNoticeClient;
import com.auth.service.system.message.model.channel.dingtalk.RenderedDingTalkNotice;
import com.auth.service.system.message.support.ChannelOptionsSupport;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;

/**
 * 钉钉工作通知渠道发送实现（统一消息编排）
 *
 * @author Bunny
 */
@Component
public class DingTalkMessageSender implements MessageSender {

	private final DingTalkTemplatePipeline dingTalkTemplatePipeline;

	private final DingTalkWorkNoticeClient workNoticeClient;

	public DingTalkMessageSender(DingTalkTemplatePipeline dingTalkTemplatePipeline,
			DingTalkWorkNoticeClient workNoticeClient) {
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

		return ObjectUtil.defaultIfNull(rendered.messageType(), DingTalkMessageType.TEXT);
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

	@Override
	public MessageChannel channel() {
		return MessageChannel.DING_TALK;
	}

	@Override
	public void sendByTemplate(TemplateMessageCommand command) {
		RenderedDingTalkNotice rendered = resolveContent(command);
		DingTalkChannelOptions options = ChannelOptionsSupport.asDingTalk(command.getOptions());
		DingTalkMessageType messageType = resolveMessageType(rendered, options);
		String title = resolveTitle(rendered, options);

		workNoticeClient.sendWorkNotice(command.getTargets(), messageType, title, rendered.content());
	}

	/**
	 * 解析发送正文：自定义正文或模板渲染
	 * @param command 发送命令
	 * @return 渲染结果
	 */
	private RenderedDingTalkNotice resolveContent(TemplateMessageCommand command) {
		// 有自定义消息体，解析自定义正文
		Boolean customBody = command.getCustomBody();
		if (BooleanUtil.isTrue(customBody)) {
			String body = command.getBody();
			if (CharSequenceUtil.isBlank(body)) {
				throw new SystemBusinessException(PARAM_REQUIRED, "钉钉消息正文");
			}

			// 解析渠道扩展参数
			DingTalkChannelOptions options = ChannelOptionsSupport.asDingTalk(command.getOptions());
			DingTalkMessageType type = options != null && options.getMessageType() != null ? options.getMessageType()
					: DingTalkMessageType.TEXT;
			String title = options != null ? options.getTitle() : null;
			return new RenderedDingTalkNotice(type, title, command.getBody());
		}

		// 没有自定义消息体，解析模板渲染
		Map<String, Object> variables = Optional.ofNullable(command.getVariables()).orElseGet(HashMap::new);
		return dingTalkTemplatePipeline.render(command.getTemplateCode(), variables);
	}

}
