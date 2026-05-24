package com.auth.service.system.message.validation;

import com.auth.module.message.api.command.ChannelOptions;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.dingtalk.DingTalkChannelOptions;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.auth.service.system.common.exception.SystemBusinessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

import static com.auth.service.system.common.exception.code.SystemMessageResultCode.MESSAGE_COMMAND_INVALID;
import static com.auth.service.system.common.exception.code.SystemMessageResultCode.MESSAGE_SEND_COMMAND_NULL;

/**
 * {@link TemplateMessageCommand} 命令式校验：Jakarta 标准约束 + 业务规则，供所有发送入口统一调用。
 *
 * @author Bunny
 */
@Component
public class TemplateMessageCommandValidation {

	/**
	 * 校验失败时的英文提示（供异常 args 或日志使用）
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
	 * 编排发送唯一校验入口（HTTP、Feign、定时任务、服务内调用）
	 * @param command 模板化发送命令
	 */
	public void validate(TemplateMessageCommand command) {
		if (command == null) {
			throw new SystemBusinessException(MESSAGE_SEND_COMMAND_NULL);
		}
		assertJakartaValid(command);
		assertBusinessRules(command);
	}

	/**
	 * 校验模板化发送命令是否符合 Jakarta 约束
	 * @param command 模板化发送命令
	 */
	private void assertJakartaValid(TemplateMessageCommand command) {
		Set<ConstraintViolation<TemplateMessageCommand>> violations = validator.validate(command);
		if (!violations.isEmpty()) {
			String message = violations.iterator().next().getMessage();
			throw new SystemBusinessException(MESSAGE_COMMAND_INVALID, message);
		}
	}

	/**
	 * 校验模板化发送命令是否符合业务规则
	 * @param command 模板化发送命令
	 */
	private void assertBusinessRules(TemplateMessageCommand command) {
		findBusinessRuleViolation(command).ifPresent(message -> {
			throw new SystemBusinessException(MESSAGE_COMMAND_INVALID, message);
		});
	}

	/**
	 * 查找首条业务规则违反说明
	 * @param command 模板化发送命令
	 * @return 违反说明；无违反时为空
	 */
	private Optional<String> findBusinessRuleViolation(TemplateMessageCommand command) {
		if (!isTemplateOrBodyValid(command)) {
			return Optional.of(INVALID_TEMPLATE_MESSAGE_COMMAND);
		}
		if (!isChannelOptionsMatch(command)) {
			return Optional.of(CHANNEL_OPTIONS_MISMATCH);
		}
		return Optional.empty();
	}

	/**
	 * 校验模板或正文是否有效
	 * @param command 模板化发送命令
	 * @return 是否有效
	 */
	private static boolean isTemplateOrBodyValid(TemplateMessageCommand command) {
		if (Boolean.TRUE.equals(command.getCustomBody())) {
			String body = command.getBody();
			return body != null && !body.isBlank();
		}
		String templateCode = command.getTemplateCode();
		return templateCode != null && !templateCode.isBlank();
	}

	/**
	 * 校验渠道选项是否匹配
	 * @param command 模板化发送命令
	 * @return 是否匹配
	 */
	private static boolean isChannelOptionsMatch(TemplateMessageCommand command) {
		if (command.getChannel() == null) {
			return true;
		}
		ChannelOptions options = command.getOptions();
		if (options == null) {
			return true;
		}
		return switch (command.getChannel()) {
			case EMAIL -> options instanceof EmailChannelOptions;
			case DING_TALK -> options instanceof DingTalkChannelOptions;
			case SMS_ALIYUN_DYPNS, IN_APP -> false;
		};
	}

}
