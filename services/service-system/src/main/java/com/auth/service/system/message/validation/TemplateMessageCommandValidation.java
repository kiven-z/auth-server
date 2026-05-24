package com.auth.service.system.message.validation;

import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemResultCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

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

	private final Validator validator;

	public TemplateMessageCommandValidation(Validator validator) {
		this.validator = validator;
	}

	/**
	 * 编排发送入口校验（HTTP 内部接口、Feign、服务内调用）
	 * @param command 模板化发送命令
	 */
	public void validateForDispatch(TemplateMessageCommand command) {
		if (command == null) {
			throw new SystemBusinessException(MESSAGE_SEND_COMMAND_NULL);
		}
		validate(command, MESSAGE_COMMAND_INVALID);
	}

	/**
	 * 定时任务消息调度入口校验
	 * @param command 模板化发送命令
	 * @param invalidCode 命令非法时的结果码
	 */
	public void validateForJob(TemplateMessageCommand command, SystemResultCode invalidCode) {
		validate(command, invalidCode);
	}

	/**
	 * Jakarta 约束与业务规则统一校验
	 * @param command 模板化发送命令
	 * @param invalidCode 非法时的结果码
	 */
	private void validate(TemplateMessageCommand command, SystemResultCode invalidCode) {
		assertJakartaValid(command, invalidCode);
		assertBusinessRules(command, invalidCode);
	}

	/**
	 * 校验模板化发送命令是否符合 Jakarta 约束
	 * @param command 模板化发送命令
	 * @param invalidCode 非法时的结果码
	 */
	private void assertJakartaValid(TemplateMessageCommand command, SystemResultCode invalidCode) {
		Set<ConstraintViolation<TemplateMessageCommand>> violations = validator.validate(command);
		if (!violations.isEmpty()) {
			String message = violations.iterator().next().getMessage();
			throw new SystemBusinessException(invalidCode, message);
		}
	}

	/**
	 * 校验模板化发送命令是否符合业务规则
	 * @param command 模板化发送命令
	 * @param invalidCode 非法时的结果码
	 */
	private void assertBusinessRules(TemplateMessageCommand command, SystemResultCode invalidCode) {
		TemplateMessageCommandChecker.findViolation(command).ifPresent(message -> {
			throw new SystemBusinessException(invalidCode, message);
		});
	}

}
