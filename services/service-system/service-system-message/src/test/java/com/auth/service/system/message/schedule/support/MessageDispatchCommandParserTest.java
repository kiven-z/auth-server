package com.auth.service.system.message.schedule.support;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.message.schedule.model.MessageDispatchPayload;
import com.auth.service.system.schedule.exception.SysJobException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.*;
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

	private MessageDispatchCommandParser parser;

	@BeforeEach
	void setUp() {
		parser = new MessageDispatchCommandParser(objectMapper, validator);
	}

	@Test
	@DisplayName("标准载荷缺少 command 时抛出 JOB_PAYLOAD_JSON_INVALID")
	void parseAndValidateReturnsCommandWhenPayloadValid() {
		String payload = """
				{
				  "version": 1
				}
				""";

		assertThatThrownBy(() -> parser.parseAndValidate(payload)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_PAYLOAD_JSON_INVALID);
	}

	@Test
	@DisplayName("标准载荷格式(version + command)解析成功")
	void parseAndValidateReturnsCommandWhenEnvelopePayloadValid() {
		String payload = """
				{
				  "version": 1,
				  "command": {
				    "channel":"EMAIL",
				    "templateCode":"LOGIN_NOTICE",
				    "targets":["dev@example.com"]
				  }
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
		assertThatThrownBy(() -> parser.parseAndValidate(" ")).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_PAYLOAD_JSON_EMPTY);
	}

	@Test
	@DisplayName("payload_json 非法 JSON 时抛出 JOB_PAYLOAD_JSON_INVALID")
	void parseAndValidateThrowsWhenPayloadMalformed() {
		assertThatThrownBy(() -> parser.parseAndValidate("{")).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_PAYLOAD_JSON_INVALID);
	}

	@Test
	@DisplayName("command 缺少 targets 时仍解析成功（命令校验由 MessageDispatchService 负责）")
	void parseAndValidateReturnsCommandWhenCommandInvalid() {
		String payload = """
				{
				  "version": 1,
				  "command": {
				    "channel":"EMAIL",
				    "templateCode":"LOGIN_NOTICE"
				  }
				}
				""";

		TemplateMessageCommand command = parser.parseAndValidate(payload);

		assertThat(command.getChannel()).isEqualTo(MessageChannel.EMAIL);
		assertThat(command.getTemplateCode()).isEqualTo("LOGIN_NOTICE");
		assertThat(command.getTargets()).isNull();
	}

	@Test
	@DisplayName("标准载荷版本不支持时抛出 JOB_PAYLOAD_VERSION_UNSUPPORTED")
	void parseAndValidateThrowsWhenPayloadVersionUnsupported() {
		String payload = """
				{
				  "version": 2,
				  "command": {
				    "channel":"EMAIL",
				    "templateCode":"LOGIN_NOTICE",
				    "targets":["dev@example.com"]
				  }
				}
				""";

		assertThatThrownBy(() -> parser.parseAndValidate(payload)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_PAYLOAD_VERSION_UNSUPPORTED);
	}

	@Test
	@DisplayName("buildTemplatePayload 返回版本化标准模板")
	void buildTemplatePayloadReturnsVersionedTemplate() {
		MessageDispatchPayload payload = parser.buildTemplatePayload();

		assertThat(payload.getVersion()).isEqualTo(MessageDispatchPayload.VERSION_1);
		assertThat(payload.getCommand()).isNotNull();
	}

}
