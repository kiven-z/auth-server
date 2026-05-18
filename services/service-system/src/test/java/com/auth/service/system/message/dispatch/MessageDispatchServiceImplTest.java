package com.auth.service.system.message.dispatch;

import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.spi.MessageSender;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * {@link MessageDispatchServiceImpl} 单元测试
 */
@DisplayName("MessageDispatchServiceImpl 消息编排")
@ExtendWith(MockitoExtension.class)
class MessageDispatchServiceImplTest {

	@Mock
	private MessageSender emailSender;

	@Mock
	private MessageSender dingTalkSender;

	private MessageDispatchServiceImpl dispatchService;

	@BeforeEach
	void setUp() {
		org.mockito.Mockito.when(emailSender.channel()).thenReturn(MessageChannel.EMAIL);
		org.mockito.Mockito.when(dingTalkSender.channel()).thenReturn(MessageChannel.DING_TALK);
		dispatchService = new MessageDispatchServiceImpl(List.of(emailSender, dingTalkSender));
	}

	@Test
	@DisplayName("EMAIL 渠道：委托对应 MessageSender")
	void sendByTemplate_routesToEmailSender() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("LOGIN_CODE")
			.targets(List.of("a@b.com"))
			.build();

		dispatchService.sendByTemplate(command);

		verify(emailSender).sendByTemplate(command);
	}

	@Test
	@DisplayName("DING_TALK 渠道：委托对应 MessageSender")
	void sendByTemplate_routesToDingTalkSender() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.DING_TALK)
			.templateCode("alert-job")
			.targets(List.of("user001"))
			.build();

		dispatchService.sendByTemplate(command);

		verify(dingTalkSender).sendByTemplate(command);
		verify(emailSender, never()).sendByTemplate(any());
	}

	@Test
	@DisplayName("未实现渠道：抛出 MESSAGE_CHANNEL_UNSUPPORTED")
	void sendByTemplate_unsupportedChannelThrows() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS_DYPNS_VERIFY_CODE)
			.templateCode("SMS_CODE")
			.targets(List.of("13800000000"))
			.build();

		assertThatThrownBy(() -> dispatchService.sendByTemplate(command)).isInstanceOf(SystemBusinessException.class);
		verify(emailSender, never()).sendByTemplate(any());
		verify(dingTalkSender, never()).sendByTemplate(any());
	}

	@Test
	@DisplayName("命令为空：抛出 MESSAGE_SEND_COMMAND_NULL")
	void sendByTemplate_nullCommandThrows() {
		assertThatThrownBy(() -> dispatchService.sendByTemplate(null)).isInstanceOf(SystemBusinessException.class);
		verify(emailSender, never()).sendByTemplate(any());
	}

}
