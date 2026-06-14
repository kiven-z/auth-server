package com.auth.service.system.message.schedule.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.message.schedule.model.MessageDispatchPayload;
import com.auth.service.system.schedule.exception.SysJobException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.*;

/**
 * 解析消息调度载荷
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
	 * 将 payload_json 解析为 TemplateMessageCommand（不校验命令业务规则）
	 * @param payloadJson 载荷 JSON
	 * @return 消息命令
	 */
	public TemplateMessageCommand parseAndValidate(String payloadJson) {
		if (CharSequenceUtil.isBlank(payloadJson)) {
			throw new SysJobException(PARAM_REQUIRED, "payload_json");
		}
		return parseCommand(payloadJson);
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
				throw new SysJobException(JOB_PAYLOAD_JSON_INVALID, "payload must contain version and command");
			}
			MessageDispatchPayload payload = objectMapper.treeToValue(root, MessageDispatchPayload.class);
			validatePayload(payload);
			return payload.getCommand();
		}
		catch (SysJobException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new SysJobException(JOB_PAYLOAD_JSON_INVALID, exception.getMessage());
		}
	}

	/**
	 * 校验标准载荷信封
	 * @param payload 标准载荷
	 */
	private void validatePayload(MessageDispatchPayload payload) {
		Set<ConstraintViolation<MessageDispatchPayload>> payloadViolations = validator.validate(payload);
		if (CollUtil.isNotEmpty(payloadViolations)) {
			String message = payloadViolations.iterator().next().getMessage();
			throw new SysJobException(JOB_PAYLOAD_JSON_INVALID, message);
		}
		if (payload.getVersion() != MessageDispatchPayload.VERSION_1) {
			throw new SysJobException(JOB_PAYLOAD_VERSION_UNSUPPORTED, String.valueOf(payload.getVersion()));
		}
	}

}
