package com.auth.service.system.message.validation;

import com.auth.module.message.api.command.ChannelOptions;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.dingtalk.DingTalkChannelOptions;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * {@link TemplateMessageCommand} 业务规则校验（实现层，非跨服务契约）
 *
 * @author Bunny
 */
@UtilityClass
public class TemplateMessageCommandChecker {

	/**
	 * 校验失败时的英文提示（供异常 args 或日志使用）
	 */
	private static final String INVALID_TEMPLATE_MESSAGE_COMMAND = "Invalid template message command";

	/**
	 * 渠道与扩展参数类型不匹配
	 */
	private static final String CHANNEL_OPTIONS_MISMATCH = "channel and options type mismatch";

	/**
	 * 查找首条业务规则违反说明
	 * @param command 模板化发送命令
	 * @return 违反说明；无违反时为空
	 */
	public static Optional<String> findViolation(TemplateMessageCommand command) {
		if (command == null) {
			return Optional.empty();
		}
		// 校验模板或正文是否有效
		if (!isTemplateOrBodyValid(command)) {
			return Optional.of(INVALID_TEMPLATE_MESSAGE_COMMAND);
		}
		// 校验渠道选项是否匹配
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
		// 校验自定义正文是否有效
		if (Boolean.TRUE.equals(command.getCustomBody())) {
			String body = command.getBody();
			return body != null && !body.isBlank();
		}

		// 校验模板代码是否有效
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
