package com.auth.module.message.api.command;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.BooleanUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * {@link TemplateMessageCommandValid} 校验实现
 *
 * @author Bunny
 */
public class TemplateMessageCommandValidator
		implements ConstraintValidator<TemplateMessageCommandValid, TemplateMessageCommand> {

	@Override
	public boolean isValid(TemplateMessageCommand command, ConstraintValidatorContext context) {
		if (command == null) {
			return true;
		}

		// customBody 为 TURE 必须有非空的 body
		if (BooleanUtil.isTrue(command.getCustomBody())) {
			return CharSequenceUtil.isNotBlank(command.getBody());
		}

		return CharSequenceUtil.isNotBlank(command.getTemplateCode());
	}

}
