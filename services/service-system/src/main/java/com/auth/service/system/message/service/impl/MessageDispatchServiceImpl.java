package com.auth.service.system.message.service.impl;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.channel.MessageSender;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.dispatch.MessageDispatchService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.validation.TemplateMessageCommandChecker;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.auth.service.system.common.exception.code.SystemMessageResultCode.MESSAGE_CHANNEL_UNSUPPORTED;
import static com.auth.service.system.common.exception.code.SystemMessageResultCode.MESSAGE_COMMAND_INVALID;
import static com.auth.service.system.common.exception.code.SystemMessageResultCode.MESSAGE_SEND_COMMAND_NULL;

/**
 * 消息发送编排实现
 *
 * @author Bunny
 */
@Service
public class MessageDispatchServiceImpl implements MessageDispatchService {

	private final Map<MessageChannel, MessageSender> senderByChannel;

	public MessageDispatchServiceImpl(List<MessageSender> senders) {
		this.senderByChannel = new EnumMap<>(MessageChannel.class);
		for (MessageSender sender : senders) {
			this.senderByChannel.put(sender.channel(), sender);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendByTemplate(TemplateMessageCommand command) {
		// 校验命令是否为空
		if (command == null) {
			throw new SystemBusinessException(MESSAGE_SEND_COMMAND_NULL);
		}
		if (command.getChannel() == null) {
			throw new SystemBusinessException(MESSAGE_CHANNEL_UNSUPPORTED, "null");
		}
		TemplateMessageCommandChecker.findViolation(command).ifPresent(message -> {
			throw new SystemBusinessException(MESSAGE_COMMAND_INVALID, message);
		});

		// 获取发送器
		MessageSender sender = senderByChannel.get(command.getChannel());
		if (sender == null) {
			throw new SystemBusinessException(MESSAGE_CHANNEL_UNSUPPORTED, command.getChannel().name());
		}
		sender.sendByTemplate(command);
	}

}
