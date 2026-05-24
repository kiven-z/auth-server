package com.auth.service.system.message.validation;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.dingtalk.DingTalkChannelOptions;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TemplateMessageCommandChecker} 单元测试
 */
@DisplayName("TemplateMessageCommandChecker 模板命令业务规则校验")
class TemplateMessageCommandCheckerTest {

	@Test
	@DisplayName("模板模式：缺少 templateCode 时校验失败")
	void findViolation_shouldFailWhenTemplateCodeMissing() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.targets(List.of("a@example.com"))
			.customBody(Boolean.FALSE)
			.build();

		assertThat(TemplateMessageCommandChecker.findViolation(command)).isPresent();
	}

	@Test
	@DisplayName("模板模式：存在 templateCode 时校验通过")
	void findViolation_shouldPassWhenTemplateCodePresent() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("login-email-code")
			.targets(List.of("a@example.com"))
			.build();

		assertThat(TemplateMessageCommandChecker.findViolation(command)).isEmpty();
	}

	@Test
	@DisplayName("自定义正文模式：缺少 body 时校验失败")
	void findViolation_shouldFailWhenCustomBodyWithoutBody() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.DING_TALK)
			.targets(List.of("user-1"))
			.customBody(Boolean.TRUE)
			.build();

		assertThat(TemplateMessageCommandChecker.findViolation(command)).isPresent();
	}

	@Test
	@DisplayName("自定义正文模式：存在 body 时校验通过")
	void findViolation_shouldPassWhenCustomBodyWithBody() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.DING_TALK)
			.targets(List.of("user-1"))
			.customBody(Boolean.TRUE)
			.body("hello")
			.build();

		assertThat(TemplateMessageCommandChecker.findViolation(command)).isEmpty();
	}

	@Test
	@DisplayName("EMAIL 渠道：EmailChannelOptions 校验通过")
	void findViolation_shouldPassForEmailWithEmailOptions() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("login-email-code")
			.targets(List.of("a@example.com"))
			.options(EmailChannelOptions.builder().hasHtml(Boolean.TRUE).build())
			.build();

		assertThat(TemplateMessageCommandChecker.findViolation(command)).isEmpty();
	}

	@Test
	@DisplayName("EMAIL 渠道：DingTalkChannelOptions 校验失败")
	void findViolation_shouldFailForEmailWithDingTalkOptions() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("login-email-code")
			.targets(List.of("a@example.com"))
			.options(DingTalkChannelOptions.builder().build())
			.build();

		assertThat(TemplateMessageCommandChecker.findViolation(command)).isPresent();
	}

	@Test
	@DisplayName("SMS_ALIYUN_DYPNS 渠道：带 options 时校验失败")
	void findViolation_shouldFailForDypnsWithAnyOptions() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS_ALIYUN_DYPNS)
			.templateCode("100001")
			.targets(List.of("13800000000"))
			.options(EmailChannelOptions.builder().build())
			.build();

		assertThat(TemplateMessageCommandChecker.findViolation(command)).isPresent();
	}

	@Test
	@DisplayName("options 为空时校验通过")
	void findViolation_shouldPassWhenOptionsNull() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS_ALIYUN_DYPNS)
			.templateCode("100001")
			.targets(List.of("13800000000"))
			.build();

		assertThat(TemplateMessageCommandChecker.findViolation(command)).isEmpty();
	}

}
