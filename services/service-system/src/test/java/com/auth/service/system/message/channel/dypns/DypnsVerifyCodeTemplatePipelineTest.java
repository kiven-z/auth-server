package com.auth.service.system.message.channel.dypns;

import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.service.system.admin.mapper.MessageTemplateMapper;
import com.auth.service.system.admin.model.entity.MessageTemplateEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.module.message.modules.sms.model.DypnsVerifyCodeSendPayload;
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
 * {@link DypnsVerifyCodeTemplatePipeline} 单元测试
 */
@DisplayName("DypnsVerifyCodeTemplatePipeline Dypns 验证码模板管道")
@ExtendWith(MockitoExtension.class)
class DypnsVerifyCodeTemplatePipelineTest {

	@Mock
	private MessageTemplateMapper messageTemplateMapper;

	private DypnsVerifyCodeTemplatePipeline pipeline;

	@BeforeEach
	void setUp() {
		pipeline = new DypnsVerifyCodeTemplatePipeline(messageTemplateMapper);
	}

	@Test
	@DisplayName("登录验证码：组装 templateParam，不包含短信签名")
	void prepare_loginCode_buildsTemplateParam() {
		MessageTemplateEntity template = new MessageTemplateEntity();
		template.setSceneCode("login-sms-code");
		template.setChannel(MessageChannel.SMS_DYPNS_VERIFY_CODE.name());
		template.setProviderTemplateCode("100001");
		template
			.setRequireFields("[{\"key\":\"code\",\"exampleValue\":\"123456\"},{\"key\":\"min\",\"exampleValue\":5}]");
		when(messageTemplateMapper.selectEnabledBySceneCodeAndChannel("login-sms-code",
				MessageChannel.SMS_DYPNS_VERIFY_CODE.name()))
			.thenReturn(template);

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.templateCode("login-sms-code")
			.targets(List.of("13800000000"))
			.variables(Map.of("code", "123456", "min", 5))
			.build();

		DypnsVerifyCodeSendPayload prepared = pipeline.prepare(command);

		assertThat(prepared.providerTemplateCode()).isEqualTo("100001");
		assertThat(prepared.templateParam()).contains("\"code\":\"123456\"");
	}

	@Test
	@DisplayName("必填变量缺失时抛出 SMS_TEMPLATE_VARS_MISSING")
	void prepare_throwsWhenRequiredVariableMissing() {
		MessageTemplateEntity template = new MessageTemplateEntity();
		template.setSceneCode("login-sms-code");
		template.setChannel(MessageChannel.SMS_DYPNS_VERIFY_CODE.name());
		template.setProviderTemplateCode("100001");
		template.setRequireFields("[{\"key\":\"code\",\"exampleValue\":\"123456\"}]");
		when(messageTemplateMapper.selectEnabledBySceneCodeAndChannel("login-sms-code",
				MessageChannel.SMS_DYPNS_VERIFY_CODE.name()))
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
