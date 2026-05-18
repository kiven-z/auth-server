package com.auth.service.system.schedule.support;

import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.List;

import static com.auth.service.system.common.exception.code.schedule.SystemScheduleResultCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link MessageDispatchCommandParser} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("MessageDispatchCommandParser 消息命令解析")
class MessageDispatchCommandParserTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	private final MessageDispatchCommandParser parser = new MessageDispatchCommandParser(objectMapper, validator);

	@Test
	@DisplayName("合法 payload_json 解析成功")
	void parseAndValidateReturnsCommandWhenPayloadValid() {
		// 构造最小可用 payload，校验可正常转为命令对象。
		String payload = """
				{
				  "channel":"EMAIL",
				  "templateCode":"LOGIN_NOTICE",
				  "targets":["dev@example.com"]
				}
				""";

		TemplateMessageCommand command = parser.parseAndValidate(payload);

		assertThat(command.getChannel()).isEqualTo(MessageChannel.EMAIL);
		assertThat(command.getTemplateCode()).isEqualTo("LOGIN_NOTICE");
		assertThat(command.getTargets()).isEqualTo(List.of("dev@example.com"));
	}

	@Test
	@DisplayName("payload_json 为空时抛出 JOB_PAYLOAD_JSON_EMPTY")
	void parseAndValidateThrowsWhenPayloadBlank() {
		// 空载荷必须被拒绝，避免运行时空指针。
		assertThatThrownBy(() -> parser.parseAndValidate(" ")).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(JOB_PAYLOAD_JSON_EMPTY);
	}

	@Test
	@DisplayName("payload_json 非法 JSON 时抛出 JOB_PAYLOAD_JSON_INVALID")
	void parseAndValidateThrowsWhenPayloadMalformed() {
		// 非法 JSON 应在解析阶段被拦截。
		assertThatThrownBy(() -> parser.parseAndValidate("{")).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(JOB_PAYLOAD_JSON_INVALID);
	}

	@Test
	@DisplayName("payload_json 违反命令约束时抛出 JOB_MESSAGE_COMMAND_INVALID")
	void parseAndValidateThrowsWhenCommandInvalid() {
		// 缺少 targets 字段会触发 jakarta 校验失败。
		String payload = """
				{
				  "channel":"EMAIL",
				  "templateCode":"LOGIN_NOTICE"
				}
				""";

		assertThatThrownBy(() -> parser.parseAndValidate(payload)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(JOB_MESSAGE_COMMAND_INVALID);
	}

}
