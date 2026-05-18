package com.auth.service.system.message.channel.sms;

import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.config.properties.SmsProperties;
import com.auth.service.system.admin.mapper.MessageTemplateMapper;
import com.auth.service.system.admin.model.entity.MessageTemplateEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.channel.sms.model.SmsPreparedMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.SMS_SIGN_NAME_NOT_CONFIGURED;
import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.SMS_TEMPLATE_VARS_MISSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * {@link SmsTemplatePipeline} 单元测试
 */
@DisplayName("SmsTemplatePipeline 短信模板管道")
@ExtendWith(MockitoExtension.class)
class SmsTemplatePipelineTest {

	@Mock
	private MessageTemplateMapper messageTemplateMapper;

	private SmsTemplatePipeline pipeline;

	@BeforeEach
	void setUp() {
		SmsProperties smsProperties = new SmsProperties();
		smsProperties.setSignName("默认签名");
		pipeline = new SmsTemplatePipeline(messageTemplateMapper, smsProperties);
	}

	@Test
	@DisplayName("登录验证码：组装 templateParam 并使用配置签名")
	void prepare_loginCode_buildsTemplateParam() {
		MessageTemplateEntity template = new MessageTemplateEntity();
		template.setSceneCode("login-sms-code");
		template.setChannel(MessageChannel.SMS.name());
		template.setProviderTemplateCode("100001");
		template
			.setRequireFields("[{\"key\":\"code\",\"exampleValue\":\"123456\"},{\"key\":\"min\",\"exampleValue\":5}]");
		when(messageTemplateMapper.selectEnabledBySceneCodeAndChannel("login-sms-code", MessageChannel.SMS.name()))
			.thenReturn(template);

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.templateCode("login-sms-code")
			.targets(List.of("13800000000"))
			.variables(Map.of("code", "123456", "min", 5))
			.build();

		SmsPreparedMessage prepared = pipeline.prepare(command);

		assertThat(prepared.providerTemplateCode()).isEqualTo("100001");
		assertThat(prepared.signName()).isEqualTo("默认签名");
		assertThat(prepared.templateParam()).contains("\"code\":\"123456\"");
	}

	@Test
	@DisplayName("未配置 signName 时抛出 SMS_SIGN_NAME_NOT_CONFIGURED")
	void prepare_withoutSignName_throws() {
		SmsProperties smsProperties = new SmsProperties();
		SmsTemplatePipeline pipelineWithoutSign = new SmsTemplatePipeline(messageTemplateMapper, smsProperties);

		MessageTemplateEntity template = new MessageTemplateEntity();
		template.setProviderTemplateCode("100001");
		template.setChannel(MessageChannel.SMS.name());
		when(messageTemplateMapper.selectEnabledBySceneCodeAndChannel("login-sms-code", MessageChannel.SMS.name()))
			.thenReturn(template);

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.templateCode("login-sms-code")
			.targets(List.of("13800000000"))
			.variables(Map.of("code", "1", "min", 5))
			.build();

		assertThatThrownBy(() -> pipelineWithoutSign.prepare(command)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SMS_SIGN_NAME_NOT_CONFIGURED);
	}

	@Test
	@DisplayName("必填变量缺失时抛出 SMS_TEMPLATE_VARS_MISSING")
	void prepare_throwsWhenRequiredVariableMissing() {
		MessageTemplateEntity template = new MessageTemplateEntity();
		template.setSceneCode("login-sms-code");
		template.setChannel(MessageChannel.SMS.name());
		template.setProviderTemplateCode("100001");
		template.setRequireFields("[{\"key\":\"code\",\"exampleValue\":\"123456\"}]");
		when(messageTemplateMapper.selectEnabledBySceneCodeAndChannel("login-sms-code", MessageChannel.SMS.name()))
			.thenReturn(template);

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.templateCode("login-sms-code")
			.targets(List.of("13800000000"))
			.variables(Map.of())
			.build();

		assertThatThrownBy(() -> pipeline.prepare(command)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SMS_TEMPLATE_VARS_MISSING);
	}

}
