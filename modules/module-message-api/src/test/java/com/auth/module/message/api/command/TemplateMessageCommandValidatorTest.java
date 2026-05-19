package com.auth.module.message.api.command;

import com.auth.module.message.api.channel.MessageChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TemplateMessageCommandValidator} 单元测试
 */
@DisplayName("TemplateMessageCommandValidator 模板命令校验")
class TemplateMessageCommandValidatorTest {

	private TemplateMessageCommandValidator validator;

	@BeforeEach
	void setUp() {
		validator = new TemplateMessageCommandValidator();
	}

	@Test
	@DisplayName("模板模式：缺少 templateCode 时校验失败")
	void isValid_shouldFailWhenTemplateCodeMissing() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.targets(List.of("a@example.com"))
			.customBody(Boolean.FALSE)
			.build();

		assertThat(validator.isValid(command, null)).isFalse();
	}

	@Test
	@DisplayName("模板模式：存在 templateCode 时校验通过")
	void isValid_shouldPassWhenTemplateCodePresent() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("login-email-code")
			.targets(List.of("a@example.com"))
			.build();

		assertThat(validator.isValid(command, null)).isTrue();
	}

	@Test
	@DisplayName("自定义正文模式：缺少 body 时校验失败")
	void isValid_shouldFailWhenCustomBodyWithoutBody() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.DING_TALK)
			.targets(List.of("user-1"))
			.customBody(Boolean.TRUE)
			.build();

		assertThat(validator.isValid(command, null)).isFalse();
	}

	@Test
	@DisplayName("自定义正文模式：存在 body 时校验通过")
	void isValid_shouldPassWhenCustomBodyWithBody() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.DING_TALK)
			.targets(List.of("user-1"))
			.customBody(Boolean.TRUE)
			.body("hello")
			.build();

		assertThat(validator.isValid(command, null)).isTrue();
	}

}
