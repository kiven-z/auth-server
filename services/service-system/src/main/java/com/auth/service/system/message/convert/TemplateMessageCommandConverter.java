package com.auth.service.system.message.convert;

import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.command.ChannelOptionsSupport;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.email.EmailChannelOptions;
import com.auth.module.message.email.EmailSendDTO;
import lombok.experimental.UtilityClass;

/**
 * {@link EmailSendDTO} 与 {@link TemplateMessageCommand} 互转
 *
 * @author Bunny
 */
@UtilityClass
public class TemplateMessageCommandConverter {

	/**
	 * 将邮件 DTO 转为统一模板发送命令
	 * @param dto 邮件发送请求
	 * @return 模板化发送命令
	 */
	public static TemplateMessageCommand fromEmailDto(EmailSendDTO dto) {
		EmailChannelOptions options = EmailChannelOptions.builder()
			.cc(dto.getCc())
			.bcc(dto.getBcc())
			.replyTo(dto.getReplyTo())
			.hasHtml(dto.getHasHtml())
			.attachments(dto.getAttachments())
			.build();
		return TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode(dto.getTemplateCode())
			.targets(dto.getTo())
			.variables(dto.getVariables())
			.options(options)
			.build();
	}

	/**
	 * 将统一命令转为邮件 DTO（仅 EMAIL 渠道有效）
	 * @param command 模板化发送命令
	 * @return 邮件发送请求
	 */
	public static EmailSendDTO toEmailDto(TemplateMessageCommand command) {
		EmailChannelOptions options = ChannelOptionsSupport.asEmail(command.getOptions());

		// 创建邮件发送请求构建器
		EmailSendDTO.EmailSendDTOBuilder builder = EmailSendDTO.builder()
			.templateCode(command.getTemplateCode())
			.to(command.getTargets())
			.variables(command.getVariables());

		// 设置邮件发送请求的配置
		if (options != null) {
			builder.cc(options.getCc())
				.bcc(options.getBcc())
				.replyTo(options.getReplyTo())
				.hasHtml(options.getHasHtml())
				.attachments(options.getAttachments());
		}
		return builder.build();
	}

}
