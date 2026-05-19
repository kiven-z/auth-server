package com.auth.service.system.schedule.support;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.schedule.model.payload.MessageDispatchPayload;
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
	@DisplayName("标准载荷缺少 command 时抛出 JOB_PAYLOAD_JSON_INVALID")
	void parseAndValidateReturnsCommandWhenPayloadValid() {
		// 重构后必须使用 version + command 结构。
		String payload = """
				{
				  "version": 1
				}
				""";

		assertThatThrownBy(() -> parser.parseAndValidate(payload)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(JOB_PAYLOAD_JSON_INVALID);
	}

	@Test
	@DisplayName("标准载荷格式(version + command)解析成功")
	void parseAndValidateReturnsCommandWhenEnvelopePayloadValid() {
		// 新协议格式应正确提取 command 字段。
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
				  "version": 1,
				  "command": {
				    "channel":"EMAIL",
				    "templateCode":"LOGIN_NOTICE"
				  }
				}
				""";

		assertThatThrownBy(() -> parser.parseAndValidate(payload)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(JOB_MESSAGE_COMMAND_INVALID);
	}

	@Test
	@DisplayName("标准载荷版本不支持时抛出 JOB_PAYLOAD_VERSION_UNSUPPORTED")
	void parseAndValidateThrowsWhenPayloadVersionUnsupported() {
		// 版本升级时可在此处扩展兼容逻辑。
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

		assertThatThrownBy(() -> parser.parseAndValidate(payload)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(JOB_PAYLOAD_VERSION_UNSUPPORTED);
	}

	@Test
	@DisplayName("buildTemplatePayload 返回版本化标准模板")
	void buildTemplatePayloadReturnsVersionedTemplate() {
		// 前端可以直接使用该模板构造 MESSAGE_DISPATCH 的 payload_json。
		MessageDispatchPayload payload = parser.buildTemplatePayload();

		assertThat(payload.getVersion()).isEqualTo(MessageDispatchPayload.VERSION_1);
		assertThat(payload.getCommand()).isNotNull();
	}

}
