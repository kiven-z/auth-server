package com.auth.service.system.message.channel.sms;

import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.config.properties.SmsBlendProperties;
import com.auth.module.message.config.properties.SmsProperties;
import com.auth.module.message.sms.SmsChannelOptions;
import com.auth.service.system.admin.mapper.SmsTemplateMapper;
import com.auth.service.system.admin.model.entity.SmsTemplateEntity;
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
	private SmsTemplateMapper smsTemplateMapper;

	private SmsTemplatePipeline pipeline;

	@BeforeEach
	void setUp() {
		SmsProperties smsProperties = new SmsProperties();
		SmsBlendProperties blend = new SmsBlendProperties();
		blend.setSignature("blend签名");
		smsProperties.getBlends().put("aliyun-main", blend);
		smsProperties.setDefaultConfigId("aliyun-main");
		pipeline = new SmsTemplatePipeline(smsTemplateMapper, smsProperties);
	}

	@Test
	@DisplayName("登录验证码：组装 templateParam 并使用 blend 签名")
	void prepare_loginCode_buildsTemplateParam() {
		SmsTemplateEntity template = new SmsTemplateEntity();
		template.setTemplateCode("login-sms-code");
		template.setProviderTemplateCode("100001");
		template
			.setRequireFields("[{\"key\":\"code\",\"exampleValue\":\"123456\"},{\"key\":\"min\",\"exampleValue\":5}]");
		when(smsTemplateMapper.selectByTemplateCode("login-sms-code")).thenReturn(template);

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.templateCode("login-sms-code")
			.targets(List.of("13800000000"))
			.variables(Map.of("code", "123456", "min", 5))
			.build();

		SmsPreparedMessage prepared = pipeline.prepare(command);

		assertThat(prepared.providerTemplateCode()).isEqualTo("100001");
		assertThat(prepared.signName()).isEqualTo("blend签名");
		assertThat(prepared.smsConfigId()).isEqualTo("aliyun-main");
		assertThat(prepared.templateParam()).contains("\"code\":\"123456\"");
	}

	@Test
	@DisplayName("模板指定 signName 与 smsConfigId 时优先使用模板值")
	void prepare_usesTemplateOverrides() {
		SmsTemplateEntity template = new SmsTemplateEntity();
		template.setTemplateCode("job-alert");
		template.setSmsConfigId("tencent-backup");
		template.setSignName("模板签名");
		template.setProviderTemplateCode("SMS_999");
		when(smsTemplateMapper.selectByTemplateCode("job-alert")).thenReturn(template);

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.templateCode("job-alert")
			.targets(List.of("13800000000"))
			.variables(Map.of())
			.build();

		SmsPreparedMessage prepared = pipeline.prepare(command);

		assertThat(prepared.signName()).isEqualTo("模板签名");
		assertThat(prepared.smsConfigId()).isEqualTo("tencent-backup");
	}

	@Test
	@DisplayName("命令 options 可覆盖 smsConfigId")
	void prepare_optionsOverrideConfigId() {
		SmsTemplateEntity template = new SmsTemplateEntity();
		template.setTemplateCode("job-alert");
		template.setProviderTemplateCode("SMS_999");
		when(smsTemplateMapper.selectByTemplateCode("job-alert")).thenReturn(template);

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.templateCode("job-alert")
			.targets(List.of("13800000000"))
			.options(SmsChannelOptions.builder().smsConfigId("aliyun-main").build())
			.build();

		SmsPreparedMessage prepared = pipeline.prepare(command);

		assertThat(prepared.smsConfigId()).isEqualTo("aliyun-main");
	}

	@Test
	@DisplayName("必填变量缺失时抛出 SMS_TEMPLATE_VARS_MISSING")
	void prepare_throwsWhenRequiredVariableMissing() {
		SmsTemplateEntity template = new SmsTemplateEntity();
		template.setTemplateCode("login-sms-code");
		template.setProviderTemplateCode("100001");
		template.setRequireFields("[{\"key\":\"code\",\"exampleValue\":\"123456\"}]");
		when(smsTemplateMapper.selectByTemplateCode("login-sms-code")).thenReturn(template);

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
