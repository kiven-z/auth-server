package com.auth.service.system.message.convert;

import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.auth.service.system.message.model.channel.email.EmailSendDTO;
import com.auth.service.system.message.support.ChannelOptionsSupport;
import lombok.experimental.UtilityClass;

/**
 * {@link EmailSendDTO} 与 {@link TemplateMessageCommand} 互转
 *
 * @author Bunny
 */
@UtilityClass
public class TemplateMessageCommandConverter {

	/**
	 * 将统一命令转为邮件 DTO（仅 EMAIL 渠道有效）
	 * @param command 模板化发送命令
	 * @return 邮件发送请求
	 */
	public static EmailSendDTO toEmailDto(TemplateMessageCommand command) {
		EmailChannelOptions options = ChannelOptionsSupport.asEmail(command.getOptions());

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

}
