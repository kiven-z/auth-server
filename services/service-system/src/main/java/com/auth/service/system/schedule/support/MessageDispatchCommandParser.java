package com.auth.service.system.schedule.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.auth.service.system.common.exception.code.schedule.SystemScheduleResultCode.*;

/**
 * 解析并校验消息调度命令
 *
 * @author Bunny
 */
@Component
public class MessageDispatchCommandParser {

	private final ObjectMapper objectMapper;

	private final Validator validator;

	public MessageDispatchCommandParser(ObjectMapper objectMapper, Validator validator) {
		this.objectMapper = objectMapper;
		this.validator = validator;
	}

	/**
	 * 将 payload_json 解析为 TemplateMessageCommand 并做 jakarta 校验
	 * @param payloadJson 载荷 JSON
	 * @return 消息命令
	 */
	public TemplateMessageCommand parseAndValidate(String payloadJson) {
		if (CharSequenceUtil.isBlank(payloadJson)) {
			throw new SystemBusinessException(JOB_PAYLOAD_JSON_EMPTY);
		}

		TemplateMessageCommand command;
		try {
			command = objectMapper.readValue(payloadJson, TemplateMessageCommand.class);
		}
		catch (Exception exception) {
			throw new SystemBusinessException(JOB_PAYLOAD_JSON_INVALID, exception.getMessage());
		}

		Set<ConstraintViolation<TemplateMessageCommand>> violations = validator.validate(command);
		if (!violations.isEmpty()) {
			String message = violations.iterator().next().getMessage();
			throw new SystemBusinessException(JOB_MESSAGE_COMMAND_INVALID, message);
		}
		return command;
	}

}
