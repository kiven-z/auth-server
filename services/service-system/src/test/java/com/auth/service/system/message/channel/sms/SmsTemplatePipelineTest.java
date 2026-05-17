package com.auth.service.system.message.channel.sms;

import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.config.properties.SmsProperties;
import com.auth.service.system.admin.mapper.SmsTemplateMapper;
import com.auth.service.system.admin.model.entity.SmsTemplateEntity;
import com.auth.service.system.message.channel.sms.model.PreparedSms;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * {@link SmsTemplatePipeline} 单元测试
 */
@DisplayName("SmsTemplatePipeline 短信模板管道")
@ExtendWith(MockitoExtension.class)
class SmsTemplatePipelineTest {

	@Mock
	private SmsTemplateMapper smsTemplateMapper;

	private SmsProperties smsProperties;

	private SmsTemplatePipeline pipeline;

	@BeforeEach
	void setUp() {
		smsProperties = new SmsProperties();
		smsProperties.getAliyun().setDefaultSignName("测试签名");
		pipeline = new SmsTemplatePipeline(smsTemplateMapper, smsProperties);
	}

	@Test
	@DisplayName("组装 templateParam JSON 与厂商模板 CODE")
	void prepare_buildsTemplateParam() {
		SmsTemplateEntity template = new SmsTemplateEntity();
		template.setTemplateCode("login-sms-code");
		template.setProviderTemplateCode("100001");
		template
			.setRequireFields("[{\"key\":\"code\",\"exampleValue\":\"123456\"},{\"key\":\"min\",\"exampleValue\":5}]");
		template.setDefaultCountryCode("86");
		when(smsTemplateMapper.selectByTemplateCode("login-sms-code")).thenReturn(template);

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.templateCode("login-sms-code")
			.targets(List.of("13800000000"))
			.variables(Map.of("code", "123456", "min", 5))
			.build();

		PreparedSms prepared = pipeline.prepare(command);

		assertThat(prepared.providerTemplateCode()).isEqualTo("100001");
		assertThat(prepared.signName()).isEqualTo("测试签名");
		assertThat(prepared.countryCode()).isEqualTo("86");
		assertThat(prepared.templateParam()).contains("\"code\":\"123456\"");
		assertThat(prepared.templateParam()).contains("\"min\":\"5\"");
	}

}
