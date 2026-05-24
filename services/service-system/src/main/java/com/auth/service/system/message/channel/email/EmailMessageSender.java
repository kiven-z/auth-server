package com.auth.service.system.message.channel.email;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.channel.MessageSender;
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageChannel channel() {
		return MessageChannel.EMAIL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendByTemplate(TemplateMessageCommand command) {
		// 将命令转换为邮件发送请求
		EmailSendDTO emailSendDTO = TemplateMessageCommandConverter.toEmailDto(command);
		// 渲染邮件模板
		Map<String, Object> variables = Optional.ofNullable(command.getVariables()).orElseGet(HashMap::new);
		// 渲染邮件模板
		RenderedEmail rendered = emailTemplatePipeline.render(command.getTemplateCode(), variables);

		mimeMailTransport.send(emailSendDTO, rendered);
	}

}
