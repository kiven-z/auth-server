package com.auth.service.system.message.service.impl;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.channel.MessageSender;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.config.MessageChannelProperties;
import com.auth.service.system.message.service.MessageDispatchService;
import com.auth.service.system.message.support.validation.TemplateMessageCommandValidation;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.auth.service.system.message.exception.MessageResultCode.MESSAGE_CHANNEL_DISABLED;
import static com.auth.service.system.message.exception.MessageResultCode.MESSAGE_CHANNEL_UNSUPPORTED;

/**
 * 消息发送编排实现
 *
 * @author Bunny
 */
@Service
public class MessageDispatchServiceImpl implements MessageDispatchService {

	private final Map<MessageChannel, MessageSender> senderByChannel;

	private final TemplateMessageCommandValidation commandValidation;

	private final MessageChannelProperties messageChannelProperties;

	public MessageDispatchServiceImpl(List<MessageSender> senders, TemplateMessageCommandValidation commandValidation,
			MessageChannelProperties messageChannelProperties) {
		this.senderByChannel = new EnumMap<>(MessageChannel.class);
		for (MessageSender sender : senders) {
			this.senderByChannel.put(sender.channel(), sender);
		}
		this.commandValidation = commandValidation;
		this.messageChannelProperties = messageChannelProperties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendByTemplate(TemplateMessageCommand command) {
		commandValidation.validate(command);
		// 校验渠道是否启用，防止发送未启用的渠道消息
		MessageChannel channel = command.getChannel();
		if (!messageChannelProperties.isEnabled(channel)) {
			throw new SystemBusinessException(MESSAGE_CHANNEL_DISABLED, channel.name());
		}

		MessageSender sender = senderByChannel.get(channel);
		if (sender == null) {
			throw new SystemBusinessException(MESSAGE_CHANNEL_UNSUPPORTED, channel.name());
		}
		sender.sendByTemplate(command);
	}

}
