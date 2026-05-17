package com.auth.service.system.message.service.impl;

import com.auth.module.message.email.EmailSendDTO;
import com.auth.service.system.message.convert.TemplateMessageCommandConverter;
import com.auth.service.system.message.dispatch.MessageDispatchService;
import com.auth.service.system.message.service.EmailSendService;
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

	@Override
	public void sendByTemplate(EmailSendDTO dto) {
		messageDispatchService.sendByTemplate(TemplateMessageCommandConverter.fromEmailDto(dto));
	}

}
