package com.auth.module.message.command;

import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.email.EmailChannelOptions;
import com.auth.module.message.sms.aliyun.SmsChannelOptions;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * {@link ChannelOptionsMatch} 校验实现
 *
 * @author Bunny
 */
public class ChannelOptionsMatchValidator implements ConstraintValidator<ChannelOptionsMatch, TemplateMessageCommand> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValid(TemplateMessageCommand command, ConstraintValidatorContext context) {
		if (command == null || command.getChannel() == null) {
			return true;
		}
		ChannelOptions options = command.getOptions();
		if (options == null) {
			return true;
		}
		return switch (command.getChannel()) {
			case EMAIL -> options instanceof EmailChannelOptions;
			case SMS -> options instanceof SmsChannelOptions;
			default -> false;
		};
	}

}
