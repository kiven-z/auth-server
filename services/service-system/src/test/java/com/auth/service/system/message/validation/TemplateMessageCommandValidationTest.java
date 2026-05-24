package com.auth.service.system.message.validation;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.dingtalk.DingTalkChannelOptions;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.auth.service.system.common.exception.SystemBusinessException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemMessageResultCode.MESSAGE_COMMAND_INVALID;
import static com.auth.service.system.common.exception.code.SystemMessageResultCode.MESSAGE_SEND_COMMAND_NULL;
import static com.auth.service.system.common.exception.code.SystemScheduleResultCode.JOB_MESSAGE_COMMAND_INVALID;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link TemplateMessageCommandValidation} 单元测试
 */
@DisplayName("TemplateMessageCommandValidation 命令式全量校验")
class TemplateMessageCommandValidationTest {

	private TemplateMessageCommandValidation commandValidation;

	@BeforeEach
	void setUp() {
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		commandValidation = new TemplateMessageCommandValidation(validator);
	}

	@Test
	@DisplayName("dispatch：command 为 null 时抛出 MESSAGE_SEND_COMMAND_NULL")
	void validateForDispatch_shouldThrowWhenCommandNull() {
		assertThatThrownBy(() -> commandValidation.validateForDispatch(null))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(MESSAGE_SEND_COMMAND_NULL);
	}

	@Test
	@DisplayName("dispatch：targets 为空时抛出 MESSAGE_COMMAND_INVALID")
	void validateForDispatch_shouldThrowWhenTargetsEmpty() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("login-email-code")
			.targets(Collections.emptyList())
			.build();

		assertThatThrownBy(() -> commandValidation.validateForDispatch(command))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(MESSAGE_COMMAND_INVALID);
	}

	@Test
	@DisplayName("dispatch：缺少 templateCode 时抛出 MESSAGE_COMMAND_INVALID")
	void validateForDispatch_shouldThrowWhenTemplateCodeMissing() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.targets(List.of("a@example.com"))
			.build();

		assertThatThrownBy(() -> commandValidation.validateForDispatch(command))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(MESSAGE_COMMAND_INVALID);
	}

	@Test
	@DisplayName("dispatch：channel 与 options 不匹配时抛出 MESSAGE_COMMAND_INVALID")
	void validateForDispatch_shouldThrowWhenChannelOptionsMismatch() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("login-email-code")
			.targets(List.of("a@example.com"))
			.options(DingTalkChannelOptions.builder().build())
			.build();

		assertThatThrownBy(() -> commandValidation.validateForDispatch(command))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(MESSAGE_COMMAND_INVALID);
	}

	@Test
	@DisplayName("dispatch：合法命令校验通过")
	void validateForDispatch_shouldPassWhenCommandValid() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("login-email-code")
			.targets(List.of("a@example.com"))
			.options(EmailChannelOptions.builder().hasHtml(Boolean.TRUE).build())
			.build();

		assertThatCode(() -> commandValidation.validateForDispatch(command)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("job：缺少 targets 时抛出 JOB_MESSAGE_COMMAND_INVALID")
	void validateForJob_shouldThrowWhenTargetsMissing() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("LOGIN_NOTICE")
			.build();

		assertThatThrownBy(() -> commandValidation.validateForJob(command, JOB_MESSAGE_COMMAND_INVALID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(JOB_MESSAGE_COMMAND_INVALID);
	}

}
