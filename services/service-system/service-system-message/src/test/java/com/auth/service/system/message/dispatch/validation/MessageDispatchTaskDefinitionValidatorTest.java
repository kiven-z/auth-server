package com.auth.service.system.message.dispatch.validation;

import com.auth.service.system.message.dispatch.support.MessageDispatchCommandParser;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_PAYLOAD_JSON_INVALID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link MessageDispatchTaskDefinitionValidator} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("MessageDispatchTaskDefinitionValidator 消息派发任务定义校验")
@ExtendWith(MockitoExtension.class)
class MessageDispatchTaskDefinitionValidatorTest {

	@Mock
	private MessageDispatchCommandParser messageDispatchCommandParser;

	private MessageDispatchTaskDefinitionValidator validator;

	@BeforeEach
	void setUp() {
		validator = new MessageDispatchTaskDefinitionValidator(messageDispatchCommandParser);
	}

	@Test
	@DisplayName("taskType：应返回 MESSAGE_DISPATCH")
	void taskType_returnsMessageDispatch() {
		assertThat(validator.taskType()).isEqualTo(SysJobTaskType.MESSAGE_DISPATCH);
	}

	@Test
	@DisplayName("validate：应委派 Parser 解析 payload_json")
	void validate_delegatesParser() {
		String payloadJson = """
				{
				  "version": 1,
				  "command": {
				    "channel":"EMAIL",
				    "templateCode":"LOGIN_NOTICE",
				    "targets":["dev@example.com"]
				  }
				}
				""";
		SysJobEntity job = new SysJobEntity();
		job.setPayloadJson(payloadJson);

		validator.validate(job);

		verify(messageDispatchCommandParser).parseAndValidate(payloadJson);
	}

	@Test
	@DisplayName("validate：Parser 抛出异常时应向上传播")
	void validate_whenParserFails_propagates() {
		String payloadJson = "bad";
		when(messageDispatchCommandParser.parseAndValidate(payloadJson))
			.thenThrow(new SysJobException(JOB_PAYLOAD_JSON_INVALID, "bad"));

		SysJobEntity job = new SysJobEntity();
		job.setPayloadJson(payloadJson);

		assertThatThrownBy(() -> validator.validate(job)).isInstanceOf(SysJobException.class);
	}

	@Test
	@DisplayName("集成：合法 payload_json 应通过解析校验")
	void integration_whenValidPayload_passes() {
		Validator beanValidator = Validation.buildDefaultValidatorFactory().getValidator();
		MessageDispatchCommandParser parser = new MessageDispatchCommandParser(new ObjectMapper(), beanValidator);
		MessageDispatchTaskDefinitionValidator integrationValidator = new MessageDispatchTaskDefinitionValidator(
				parser);

		SysJobEntity job = new SysJobEntity();
		job.setPayloadJson("""
				{
				  "version": 1,
				  "command": {
				    "channel":"EMAIL",
				    "templateCode":"LOGIN_NOTICE",
				    "targets":["dev@example.com"]
				  }
				}
				""");

		assertThatCode(() -> integrationValidator.validate(job)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("集成：缺少 command 应抛出 SysJobException")
	void integration_whenCommandMissing_throws() {
		Validator beanValidator = Validation.buildDefaultValidatorFactory().getValidator();
		MessageDispatchCommandParser parser = new MessageDispatchCommandParser(new ObjectMapper(), beanValidator);
		MessageDispatchTaskDefinitionValidator integrationValidator = new MessageDispatchTaskDefinitionValidator(
				parser);

		SysJobEntity job = new SysJobEntity();
		job.setPayloadJson("{\"version\":1}");

		assertThatThrownBy(() -> integrationValidator.validate(job)).isInstanceOf(SysJobException.class);
	}

}
