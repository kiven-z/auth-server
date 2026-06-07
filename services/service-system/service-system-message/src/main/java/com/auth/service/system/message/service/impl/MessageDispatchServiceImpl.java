package com.auth.service.system.message.service.impl;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.channel.MessageSender;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.service.MessageDispatchService;
import com.auth.service.system.message.validation.TemplateMessageCommandValidation;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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

	public MessageDispatchServiceImpl(List<MessageSender> senders, TemplateMessageCommandValidation commandValidation) {
		this.senderByChannel = new EnumMap<>(MessageChannel.class);
		for (MessageSender sender : senders) {
			this.senderByChannel.put(sender.channel(), sender);
		}
		this.commandValidation = commandValidation;
	}

	@Override
	public void sendByTemplate(TemplateMessageCommand command) {
		commandValidation.validate(command);

		MessageSender sender = senderByChannel.get(command.getChannel());
		if (sender == null) {
			throw new SystemBusinessException(MESSAGE_CHANNEL_UNSUPPORTED, command.getChannel().name());
		}
		sender.sendByTemplate(command);
	}

}
