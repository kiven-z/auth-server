package com.auth.service.system.message.api.impl;

import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.email.EmailSendDTO;
import com.auth.service.system.message.api.EmailSendService;
import com.auth.service.system.message.api.MessageDispatchService;
import com.auth.service.system.message.convert.TemplateMessageCommandConverter;
import org.springframework.stereotype.Service;

/**
 * 邮件发送服务实现（委托统一消息编排，保留原有 API 兼容）
 *
 * @author Bunny
 */
@Service
public class EmailSendServiceImpl implements EmailSendService {

	private final MessageDispatchService messageDispatchService;

	public EmailSendServiceImpl(MessageDispatchService messageDispatchService) {
		this.messageDispatchService = messageDispatchService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendByTemplate(EmailSendDTO dto) {
		TemplateMessageCommand command = TemplateMessageCommandConverter.fromEmailDto(dto);
		messageDispatchService.sendByTemplate(command);
	}

}
