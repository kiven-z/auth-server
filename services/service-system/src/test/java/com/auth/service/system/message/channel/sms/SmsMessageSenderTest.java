package com.auth.service.system.message.channel.sms;

import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.modules.sms.SmsSender;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.channel.sms.model.SmsPreparedMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.SMS_SENDER_NOT_ENABLED;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link SmsMessageSender} 单元测试
 */
@DisplayName("SmsMessageSender 短信发送")
@ExtendWith(MockitoExtension.class)
class SmsMessageSenderTest {

	@Mock
	private SmsTemplatePipeline smsTemplatePipeline;

	@Mock
	private ObjectProvider<SmsSender> smsSender;

	private SmsMessageSender smsMessageSender;

	@BeforeEach
	void setUp() {
		smsMessageSender = new SmsMessageSender(smsTemplatePipeline, smsSender);
	}

	@Test
	@DisplayName("按模板向每个手机号发送短信")
	void sendByTemplate_invokesSenderPerTarget() {
		SmsSender sender = mock(SmsSender.class);
		when(smsSender.getIfAvailable()).thenReturn(sender);
		SmsPreparedMessage prepared = new SmsPreparedMessage("100001", "签名", "{\"code\":\"1\"}");
		when(smsTemplatePipeline.prepare(org.mockito.ArgumentMatchers.any())).thenReturn(prepared);

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS)
			.templateCode("login-sms-code")
			.targets(List.of("13800000001", "13800000002"))
			.build();

		smsMessageSender.sendByTemplate(command);

		verify(sender).sendVerifyCode(eq("13800000001"), eq("签名"), eq("100001"), eq("{\"code\":\"1\"}"));
		verify(sender).sendVerifyCode(eq("13800000002"), eq("签名"), eq("100001"), eq("{\"code\":\"1\"}"));
	}

	@Test
	@DisplayName("未装配 SmsSender：抛出 SMS_SENDER_NOT_ENABLED")
	void sendByTemplate_withoutSender_throws() {
		SmsPreparedMessage prepared = new SmsPreparedMessage("100001", "签名", "{}");
		when(smsTemplatePipeline.prepare(org.mockito.ArgumentMatchers.any())).thenReturn(prepared);

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.templateCode("job-alert")
			.targets(List.of("13800000001"))
			.build();

		assertThatThrownBy(() -> smsMessageSender.sendByTemplate(command)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SMS_SENDER_NOT_ENABLED);
	}

}
