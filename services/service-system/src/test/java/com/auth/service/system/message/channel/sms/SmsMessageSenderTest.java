package com.auth.service.system.message.channel.sms;

import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.sms.aliyun.SmsVerifyCodeSender;
import com.auth.service.system.message.channel.sms.model.PreparedSms;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SmsMessageSender} 单元测试
 */
@DisplayName("SmsMessageSender 短信发送")
@ExtendWith(MockitoExtension.class)
class SmsMessageSenderTest {

	@Mock
	private SmsTemplatePipeline smsTemplatePipeline;

	@Mock
	private SmsVerifyCodeSender smsVerifyCodeSender;

	@InjectMocks
	private SmsMessageSender smsMessageSender;

	@Test
	@DisplayName("按模板向每个手机号发送")
	void sendByTemplate_invokesSenderPerTarget() {
		PreparedSms prepared = new PreparedSms("100001", "签名", "{\"code\":\"1\"}", "86", null);
		when(smsTemplatePipeline.prepare(org.mockito.ArgumentMatchers.any())).thenReturn(prepared);

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS)
			.templateCode("login-sms-code")
			.targets(List.of("13800000001", "13800000002"))
			.variables(Map.of("code", "1", "min", 5))
			.build();

		smsMessageSender.sendByTemplate(command);

		verify(smsVerifyCodeSender).sendVerifyCode(eq("13800000001"), eq("100001"), eq("签名"), eq("{\"code\":\"1\"}"),
				eq("86"), isNull());
		verify(smsVerifyCodeSender).sendVerifyCode(eq("13800000002"), eq("100001"), eq("签名"), eq("{\"code\":\"1\"}"),
				eq("86"), isNull());
	}

}
