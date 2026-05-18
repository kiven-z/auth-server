package com.auth.service.system.message.api.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.modules.dingtalk.DingTalkChannelOptions;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.api.DingTalkWorkNoticeService;
import com.auth.service.system.message.api.MessageDispatchService;
import com.auth.service.system.message.channel.dingtalk.model.DingTalkWorkNoticeSendForm;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.DING_TALK_TARGET_EMPTY;

/**
 * 钉钉工作通知发送实现（委托统一消息编排，保留原表单 API 兼容）
 *
 * @author Bunny
 */
@ConditionalOnProperty(prefix = "auth.message.ding-talk", name = "enabled", havingValue = "true")
@Service
public class DingTalkWorkNoticeServiceImpl implements DingTalkWorkNoticeService {

	private final MessageDispatchService messageDispatchService;

	public DingTalkWorkNoticeServiceImpl(MessageDispatchService messageDispatchService) {
		this.messageDispatchService = messageDispatchService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void send(DingTalkWorkNoticeSendForm form) {
		if (form == null || CollUtil.isEmpty(form.getUserIds())) {
			throw new SystemBusinessException(DING_TALK_TARGET_EMPTY);
		}

		DingTalkChannelOptions options = DingTalkChannelOptions.builder()
			.title(form.getTitle())
			.messageType(form.getMessageType())
			.build();

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.DING_TALK)
			.targets(form.getUserIds())
			.customBody(Boolean.TRUE)
			.body(form.getContent())
			.options(options)
			.build();

		messageDispatchService.sendByTemplate(command);
	}

}
