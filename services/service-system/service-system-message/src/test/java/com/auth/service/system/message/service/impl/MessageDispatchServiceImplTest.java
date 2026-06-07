package com.auth.service.system.message.service.impl;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.channel.MessageSender;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.validation.TemplateMessageCommandValidation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static com.auth.service.system.message.exception.MessageResultCode.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link MessageDispatchServiceImpl} 单元测试
 */
@DisplayName("MessageDispatchServiceImpl 消息渠道路由")
@ExtendWith(MockitoExtension.class)
class MessageDispatchServiceImplTest {

	@Mock
	private MessageSender emailSender;

	@Mock
	private MessageSender dingTalkSender;

	private MessageDispatchServiceImpl dispatchService;

	@BeforeEach
	void setUp() {
		when(emailSender.channel()).thenReturn(MessageChannel.EMAIL);
		when(dingTalkSender.channel()).thenReturn(MessageChannel.DING_TALK);
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		TemplateMessageCommandValidation commandValidation = new TemplateMessageCommandValidation(validator);
		dispatchService = new MessageDispatchServiceImpl(List.of(emailSender, dingTalkSender), commandValidation);
	}

	@Test
	@DisplayName("EMAIL 渠道：委托对应 MessageSender")
	void sendByTemplate_shouldRouteToEmailSender() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("login-email-code")
			.targets(List.of("a@example.com"))
			.build();

		dispatchService.sendByTemplate(command);

		verify(emailSender).sendByTemplate(command);
		verify(dingTalkSender, never()).sendByTemplate(any());
	}

	@Test
	@DisplayName("DING_TALK 渠道：委托对应 MessageSender")
	void sendByTemplate_shouldRouteToDingTalkSender() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.DING_TALK)
			.templateCode("alert")
			.targets(List.of("user-1"))
			.build();

		dispatchService.sendByTemplate(command);

		verify(dingTalkSender).sendByTemplate(command);
		verify(emailSender, never()).sendByTemplate(any());
	}

	@Test
	@DisplayName("不支持的渠道：抛出 SystemBusinessException")
	void sendByTemplate_shouldThrowWhenChannelUnsupported() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS_ALIYUN_DYPNS)
			.templateCode("100001")
			.targets(List.of("13800000000"))
			.build();

		assertThatThrownBy(() -> dispatchService.sendByTemplate(command)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(MESSAGE_CHANNEL_UNSUPPORTED);

		verify(emailSender, never()).sendByTemplate(any());
		verify(dingTalkSender, never()).sendByTemplate(any());
	}

	@Test
	@DisplayName("command 为 null：抛出 SystemBusinessException")
	void sendByTemplate_shouldThrowWhenCommandNull() {
		assertThatThrownBy(() -> dispatchService.sendByTemplate(null)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(MESSAGE_SEND_COMMAND_NULL);
	}

	@Test
	@DisplayName("targets 为空：抛出 MESSAGE_COMMAND_INVALID")
	void sendByTemplate_shouldThrowWhenTargetsEmpty() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("login-email-code")
			.targets(Collections.emptyList())
			.build();

		assertThatThrownBy(() -> dispatchService.sendByTemplate(command)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(MESSAGE_COMMAND_INVALID);

		verify(emailSender, never()).sendByTemplate(any());
	}

}
