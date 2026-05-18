package com.auth.module.message.command;

import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.email.EmailChannelOptions;
import com.auth.module.message.sms.aliyun.SmsChannelOptions;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ChannelOptionsMatchValidator} 单元测试
 */
@DisplayName("ChannelOptionsMatchValidator 渠道与扩展参数一致性")
class ChannelOptionsMatchValidatorTest {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	@DisplayName("EMAIL 渠道 + 邮件 options：通过")
	void emailChannelWithEmailOptions_valid() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("T1")
			.targets(List.of("u@example.com"))
			.options(EmailChannelOptions.builder().hasHtml(true).build())
			.build();

		Set<ConstraintViolation<TemplateMessageCommand>> violations = validator.validate(command);
		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("SMS 渠道 + 邮件 options：校验失败")
	void smsChannelWithEmailOptions_invalid() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS)
			.templateCode("T1")
			.targets(List.of("13800000000"))
			.options(EmailChannelOptions.builder().build())
			.build();

		Set<ConstraintViolation<TemplateMessageCommand>> violations = validator.validate(command);
		assertThat(violations).extracting(ConstraintViolation::getMessage)
			.contains("channel and options type mismatch");
	}

	@Test
	@DisplayName("SMS 渠道 + 短信 options：通过")
	void smsChannelWithSmsOptions_valid() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS)
			.templateCode("T1")
			.targets(List.of("13800000000"))
			.options(SmsChannelOptions.builder().countryCode("86").build())
			.build();

		Set<ConstraintViolation<TemplateMessageCommand>> violations = validator.validate(command);
		assertThat(violations).isEmpty();
	}

}
