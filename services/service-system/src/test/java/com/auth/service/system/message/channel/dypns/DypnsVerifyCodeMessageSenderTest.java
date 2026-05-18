package com.auth.service.system.message.channel.dypns;

import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.modules.sms.DypnsVerifyCodeSender;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.channel.dypns.model.DypnsVerifyCodePreparedMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.SMS_CUSTOM_BODY_NOT_SUPPORTED;
import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.SMS_SENDER_NOT_ENABLED;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link DypnsVerifyCodeMessageSender} 单元测试
 */
@DisplayName("DypnsVerifyCodeMessageSender Dypns 验证码短信发送")
@ExtendWith(MockitoExtension.class)
class DypnsVerifyCodeMessageSenderTest {

	@Mock
	private DypnsVerifyCodeTemplatePipeline dypnsVerifyCodeTemplatePipeline;

	@Mock
	private ObjectProvider<DypnsVerifyCodeSender> dypnsVerifyCodeSender;

	private DypnsVerifyCodeMessageSender messageSender;

	@BeforeEach
	void setUp() {
		messageSender = new DypnsVerifyCodeMessageSender(dypnsVerifyCodeTemplatePipeline, dypnsVerifyCodeSender);
	}

	@Test
	@DisplayName("按模板向每个手机号发送 Dypns 验证码短信")
	void sendByTemplate_invokesSenderPerTarget() {
		DypnsVerifyCodeSender sender = mock(DypnsVerifyCodeSender.class);
		when(dypnsVerifyCodeSender.getIfAvailable()).thenReturn(sender);
		DypnsVerifyCodePreparedMessage prepared = new DypnsVerifyCodePreparedMessage("100001", "{\"code\":\"1\"}");
		when(dypnsVerifyCodeTemplatePipeline.prepare(org.mockito.ArgumentMatchers.any())).thenReturn(prepared);

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS_DYPNS_VERIFY_CODE)
			.templateCode("login-sms-code")
			.targets(List.of("13800000001", "13800000002"))
			.build();

		messageSender.sendByTemplate(command);

		verify(sender).sendVerifyCode(eq("13800000001"), eq("100001"), eq("{\"code\":\"1\"}"));
		verify(sender).sendVerifyCode(eq("13800000002"), eq("100001"), eq("{\"code\":\"1\"}"));
	}

	@Test
	@DisplayName("未装配 DypnsVerifyCodeSender：抛出 SMS_SENDER_NOT_ENABLED")
	void sendByTemplate_withoutSender_throws() {
		DypnsVerifyCodePreparedMessage prepared = new DypnsVerifyCodePreparedMessage("100001", "{}");
		when(dypnsVerifyCodeTemplatePipeline.prepare(org.mockito.ArgumentMatchers.any())).thenReturn(prepared);

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS_DYPNS_VERIFY_CODE)
			.templateCode("job-alert")
			.targets(List.of("13800000001"))
			.build();

		assertThatThrownBy(() -> messageSender.sendByTemplate(command)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SMS_SENDER_NOT_ENABLED);
	}

	@Test
	@DisplayName("自定义正文：SMS_DYPNS_VERIFY_CODE 渠道拒绝")
	void sendByTemplate_customBody_throws() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS_DYPNS_VERIFY_CODE)
			.targets(List.of("13800000001"))
			.customBody(Boolean.TRUE)
			.body("raw")
			.build();

		assertThatThrownBy(() -> messageSender.sendByTemplate(command)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SMS_CUSTOM_BODY_NOT_SUPPORTED);
		verify(dypnsVerifyCodeTemplatePipeline, never()).prepare(any());
	}

}
