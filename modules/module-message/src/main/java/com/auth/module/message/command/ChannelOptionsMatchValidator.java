package com.auth.module.message.command;

import com.auth.module.message.modules.dingtalk.DingTalkChannelOptions;
import com.auth.module.message.modules.email.EmailChannelOptions;
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
			case DING_TALK -> options instanceof DingTalkChannelOptions;
			// Dypns 验证码短信（无 ChannelOptions）
			case SMS_DYPNS_VERIFY_CODE -> false;
			case IN_APP -> false;
		};
	}

}
