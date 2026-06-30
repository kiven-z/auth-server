package com.auth.service.system.message.support.validation;

import java.util.Set;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.BooleanUtil;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.ChannelOptions;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.dingtalk.DingTalkChannelOptions;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.auth.service.system.common.exception.SystemBusinessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.springframework.stereotype.Component;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.message.exception.MessageResultCode.MESSAGE_COMMAND_INVALID;

/**
 * 统一发送命令入参校验
 *
 * @author Bunny
 */
@Component
public class TemplateMessageCommandValidation {

	/**
	 * 模板/正文互斥规则不满足
	 */
	private static final String INVALID_TEMPLATE_MESSAGE_COMMAND = "Invalid template message command";

	/**
	 * 渠道与扩展参数类型不匹配
	 */
	private static final String CHANNEL_OPTIONS_MISMATCH = "channel and options type mismatch";

	private final Validator validator;

	public TemplateMessageCommandValidation(Validator validator) {
		this.validator = validator;
	}

	/**
	 * 校验模板或正文是否有效
	 * @param command 模板化发送命令
	 * @return 是否有效
	 */
	private static boolean isTemplateOrBodyValid(TemplateMessageCommand command) {
		if (BooleanUtil.isTrue(command.getCustomBody())) {
			return CharSequenceUtil.isNotBlank(command.getBody());
		}
		return CharSequenceUtil.isNotBlank(command.getTemplateCode());
	}

	/**
	 * 判断渠道与扩展参数类型是否匹配（options 为空时视为匹配）
	 * @param channel 发送渠道
	 * @param options 渠道扩展参数
	 * @return 是否匹配
	 */
	public static boolean matches(MessageChannel channel, ChannelOptions options) {
		if (channel == null || options == null) {
			return true;
		}

		return switch (channel) {
			case EMAIL -> options instanceof EmailChannelOptions;
			case DING_TALK -> options instanceof DingTalkChannelOptions;
			case SMS_ALIYUN_DYPNS, IN_APP -> false;
		};
	}

	/**
	 * 校验模板化发送命令是否符合业务规则
	 * @param command 模板化发送命令
	 */
	public void validate(TemplateMessageCommand command) {
		if (command == null) {
			throw new SystemBusinessException(PARAM_REQUIRED, "消息发送参数");
		}

		// 校验命令是否符合标准约束
		Set<ConstraintViolation<TemplateMessageCommand>> violations = validator.validate(command);
		if (CollUtil.isNotEmpty(violations)) {
			throw new SystemBusinessException(MESSAGE_COMMAND_INVALID, CollUtil.getFirst(violations).getMessage());
		}

		// 校验模板或正文是否有效
		if (!isTemplateOrBodyValid(command)) {
			throw new SystemBusinessException(MESSAGE_COMMAND_INVALID, INVALID_TEMPLATE_MESSAGE_COMMAND);
		}

		// 校验渠道与扩展参数类型是否匹配
		if (!matches(command.getChannel(), command.getOptions())) {
			throw new SystemBusinessException(MESSAGE_COMMAND_INVALID, CHANNEL_OPTIONS_MISMATCH);
		}
	}

}
