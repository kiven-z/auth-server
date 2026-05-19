package com.auth.module.message.api.command;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.model.dingtalk.DingTalkChannelOptions;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ChannelOptionsMatchValidator} 单元测试
 */
@DisplayName("ChannelOptionsMatchValidator 渠道扩展参数匹配校验")
class ChannelOptionsMatchValidatorTest {

	private ChannelOptionsMatchValidator validator;

	@BeforeEach
	void setUp() {
		validator = new ChannelOptionsMatchValidator();
	}

	@Test
	@DisplayName("EMAIL 渠道：EmailChannelOptions 校验通过")
	void isValid_shouldPassForEmailWithEmailOptions() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("login-email-code")
			.targets(List.of("a@example.com"))
			.options(EmailChannelOptions.builder().hasHtml(Boolean.TRUE).build())
			.build();

		assertThat(validator.isValid(command, null)).isTrue();
	}

	@Test
	@DisplayName("EMAIL 渠道：DingTalkChannelOptions 校验失败")
	void isValid_shouldFailForEmailWithDingTalkOptions() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("login-email-code")
			.targets(List.of("a@example.com"))
			.options(DingTalkChannelOptions.builder().build())
			.build();

		assertThat(validator.isValid(command, null)).isFalse();
	}

	@Test
	@DisplayName("SMS_ALIYUN_DYPNS 渠道：带 options 时校验失败")
	void isValid_shouldFailForDypnsWithAnyOptions() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS_ALIYUN_DYPNS)
			.templateCode("100001")
			.targets(List.of("13800000000"))
			.options(EmailChannelOptions.builder().build())
			.build();

		assertThat(validator.isValid(command, null)).isFalse();
	}

	@Test
	@DisplayName("options 为空时校验通过")
	void isValid_shouldPassWhenOptionsNull() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS_ALIYUN_DYPNS)
			.templateCode("100001")
			.targets(List.of("13800000000"))
			.build();

		assertThat(validator.isValid(command, null)).isTrue();
	}

}
