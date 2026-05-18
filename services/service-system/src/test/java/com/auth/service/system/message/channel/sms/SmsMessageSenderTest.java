package com.auth.service.system.message.channel.sms;

import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.sms.SmsNotificationSender;
import com.auth.module.message.sms.SmsSendMode;
import com.auth.module.message.sms.aliyun.SmsVerifyCodeSender;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.channel.sms.model.PreparedSms;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Map;

import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.SMS_NOTIFICATION_NOT_ENABLED;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
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
	private ObjectProvider<SmsVerifyCodeSender> smsVerifyCodeSender;

	@Mock
	private ObjectProvider<SmsNotificationSender> smsNotificationSender;

	private SmsMessageSender smsMessageSender;

	@BeforeEach
	void setUp() {
		smsMessageSender = new SmsMessageSender(smsTemplatePipeline, smsVerifyCodeSender, smsNotificationSender);
	}

	@Test
	@DisplayName("VERIFY 模式：按模板向每个手机号发送验证码")
	void sendByTemplate_verify_invokesSenderPerTarget() {
		SmsVerifyCodeSender verifySender = mock(SmsVerifyCodeSender.class);
		when(smsVerifyCodeSender.getIfAvailable()).thenReturn(verifySender);
		PreparedSms prepared = new PreparedSms(SmsSendMode.VERIFY, "100001", "签名", "{\"code\":\"1\"}", "86", null,
				null);
		when(smsTemplatePipeline.prepare(org.mockito.ArgumentMatchers.any())).thenReturn(prepared);

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS)
			.templateCode("login-sms-code")
			.targets(List.of("13800000001", "13800000002"))
			.variables(Map.of("code", "1", "min", 5))
			.build();

		smsMessageSender.sendByTemplate(command);

		verify(verifySender).sendVerifyCode(eq("13800000001"), eq("100001"), eq("签名"), eq("{\"code\":\"1\"}"), eq("86"),
				isNull());
		verify(verifySender).sendVerifyCode(eq("13800000002"), eq("100001"), eq("签名"), eq("{\"code\":\"1\"}"), eq("86"),
				isNull());
	}

	@Test
	@DisplayName("NOTIFY 模式：调用 SmsNotificationSender")
	void sendByTemplate_notify_invokesNotificationSender() {
		SmsNotificationSender notificationSender = mock(SmsNotificationSender.class);
		when(smsNotificationSender.getIfAvailable()).thenReturn(notificationSender);
		PreparedSms prepared = new PreparedSms(SmsSendMode.NOTIFY, "SMS_123", "签名", "{\"code\":\"1\"}", "86", null,
				"aliyun-main");
		when(smsTemplatePipeline.prepare(org.mockito.ArgumentMatchers.any())).thenReturn(prepared);

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS)
			.templateCode("job-alert")
			.targets(List.of("13800000001"))
			.build();

		smsMessageSender.sendByTemplate(command);

		verify(notificationSender).sendTemplateSms(eq("13800000001"), eq("SMS_123"), eq("签名"), eq("{\"code\":\"1\"}"),
				eq("aliyun-main"));
	}

	@Test
	@DisplayName("NOTIFY 模式但未装配通知发送器：抛出 SMS_NOTIFICATION_NOT_ENABLED")
	void sendByTemplate_notify_withoutSender_throws() {
		PreparedSms prepared = new PreparedSms(SmsSendMode.NOTIFY, "SMS_123", "签名", "{}", "86", null, null);
		when(smsTemplatePipeline.prepare(org.mockito.ArgumentMatchers.any())).thenReturn(prepared);

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.templateCode("job-alert")
			.targets(List.of("13800000001"))
			.build();

		assertThatThrownBy(() -> smsMessageSender.sendByTemplate(command)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SMS_NOTIFICATION_NOT_ENABLED);
	}

}
