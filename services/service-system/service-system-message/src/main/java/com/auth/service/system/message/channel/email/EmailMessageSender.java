package com.auth.service.system.message.channel.email;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.channel.MessageSender;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.auth.service.system.message.channel.email.transport.MimeMailTransport;
import com.auth.service.system.message.model.channel.email.EmailSendDTO;
import com.auth.service.system.message.model.channel.email.RenderedEmail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 电子邮件渠道发送实现
 *
 * @author Bunny
 */
@Slf4j
@Component
public class EmailMessageSender implements MessageSender {

	private final EmailTemplatePipeline emailTemplatePipeline;

	private final MimeMailTransport mimeMailTransport;

	public EmailMessageSender(EmailTemplatePipeline emailTemplatePipeline, MimeMailTransport mimeMailTransport) {
		this.emailTemplatePipeline = emailTemplatePipeline;
		this.mimeMailTransport = mimeMailTransport;
	}

	/**
	 * 将统一命令转为邮件 DTO（仅 EMAIL 渠道有效）
	 * @param command 模板化发送命令
	 * @return 邮件发送请求
	 */
	private static EmailSendDTO toEmailDto(TemplateMessageCommand command) {
		// 转为邮件扩展参数
		EmailChannelOptions options = command.getOptions() instanceof EmailChannelOptions email ? email : null;

		EmailSendDTO.EmailSendDTOBuilder builder = EmailSendDTO.builder()
			.templateCode(command.getTemplateCode())
			.to(command.getTargets())
			.variables(command.getVariables());

		if (options != null) {
			builder.cc(options.getCc())
				.bcc(options.getBcc())
				.replyTo(options.getReplyTo())
				.hasHtml(options.getHasHtml())
				.attachments(options.getAttachments());
		}
		return builder.build();
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
		EmailSendDTO emailSendDTO = toEmailDto(command);
		Map<String, Object> variables = Optional.ofNullable(command.getVariables()).orElseGet(HashMap::new);
		RenderedEmail rendered = emailTemplatePipeline.render(command.getTemplateCode(), variables);

		mimeMailTransport.send(emailSendDTO, rendered);
	}

}
