package com.auth.service.system.schedule.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.schedule.model.payload.MessageDispatchPayload;
import com.fasterxml.jackson.databind.JsonNode;
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
		TemplateMessageCommand command = parseCommand(payloadJson);
		validateCommand(command);
		return command;
	}

	/**
	 * 构建标准消息调度载荷模板
	 * @return 标准载荷模板
	 */
	public MessageDispatchPayload buildTemplatePayload() {
		MessageDispatchPayload payload = new MessageDispatchPayload();
		payload.setVersion(MessageDispatchPayload.VERSION_1);
		payload.setCommand(new TemplateMessageCommand());
		return payload;
	}

	/**
	 * 解析载荷并提取消息命令
	 * @param payloadJson 载荷 JSON
	 * @return 消息命令
	 */
	private TemplateMessageCommand parseCommand(String payloadJson) {
		try {
			JsonNode root = objectMapper.readTree(payloadJson);
			if (root == null || !root.has("command")) {
				throw new SystemBusinessException(JOB_PAYLOAD_JSON_INVALID, "payload must contain version and command");
			}
			MessageDispatchPayload payload = objectMapper.treeToValue(root, MessageDispatchPayload.class);
			validatePayload(payload);
			return payload.getCommand();
		}
		catch (SystemBusinessException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new SystemBusinessException(JOB_PAYLOAD_JSON_INVALID, exception.getMessage());
		}
	}

	/**
	 * 校验标准载荷
	 * @param payload 标准载荷
	 */
	private void validatePayload(MessageDispatchPayload payload) {
		Set<ConstraintViolation<MessageDispatchPayload>> payloadViolations = validator.validate(payload);
		if (!payloadViolations.isEmpty()) {
			String message = payloadViolations.iterator().next().getMessage();
			throw new SystemBusinessException(JOB_PAYLOAD_JSON_INVALID, message);
		}
		if (payload.getVersion() != MessageDispatchPayload.VERSION_1) {
			throw new SystemBusinessException(JOB_PAYLOAD_VERSION_UNSUPPORTED, String.valueOf(payload.getVersion()));
		}
	}

	/**
	 * 校验消息命令
	 * @param command 消息命令
	 */
	private void validateCommand(TemplateMessageCommand command) {
		Set<ConstraintViolation<TemplateMessageCommand>> violations = validator.validate(command);
		if (!violations.isEmpty()) {
			String message = violations.iterator().next().getMessage();
			throw new SystemBusinessException(JOB_MESSAGE_COMMAND_INVALID, message);
		}
	}

}
