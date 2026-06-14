package com.auth.service.system.message.channel.email;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.channel.MessageSender;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.message.channel.email.transport.MimeMailTransport;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 电子邮件渠道发送实现
 *
 * @author Bunny
 */
@Component
public class EmailMessageSender implements MessageSender {

	private final EmailTemplatePipeline emailTemplatePipeline;

	private final MimeMailTransport mimeMailTransport;

	public EmailMessageSender(EmailTemplatePipeline emailTemplatePipeline, MimeMailTransport mimeMailTransport) {
		this.emailTemplatePipeline = emailTemplatePipeline;
		this.mimeMailTransport = mimeMailTransport;
	}

	@Override
	public MessageChannel channel() {
		return MessageChannel.EMAIL;
	}

	@Override
	public void sendByTemplate(TemplateMessageCommand command) {
		EmailSendDTO emailSendDTO = TemplateMessageCommandConverter.toEmailDto(command);
		Map<String, Object> variables = Optional.ofNullable(command.getVariables()).orElseGet(HashMap::new);
		RenderedEmail rendered = emailTemplatePipeline.render(command.getTemplateCode(), variables);
		mimeMailTransport.send(emailSendDTO, rendered);
	}

}
