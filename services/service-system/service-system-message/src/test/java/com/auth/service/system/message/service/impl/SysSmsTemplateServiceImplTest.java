package com.auth.service.system.message.service.impl;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.service.system.message.mapper.MessageTemplateMapper;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.form.sms.SmsTemplateForm;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysSmsTemplateServiceImpl} 短信模板创建单元测试
 */
@DisplayName("SysSmsTemplateServiceImpl 短信模板服务")
@ExtendWith(MockitoExtension.class)
class SysSmsTemplateServiceImplTest {

	@Mock
	private MessageTemplateMapper messageTemplateMapper;

	@InjectMocks
	private SysSmsTemplateServiceImpl sysSmsTemplateService;

	@BeforeAll
	static void initMybatisPlusTableInfo() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
				MessageTemplateEntity.class);
	}

	@Test
	@DisplayName("创建：写入 SMS 渠道、厂商模板码，并初始化空 require_fields")
	void create_shouldPersistSmsChannelAndProviderCode() {
		when(messageTemplateMapper.insert(any(MessageTemplateEntity.class))).thenReturn(1);

		SmsTemplateForm form = new SmsTemplateForm();
		form.setTemplateCode("sms_login");
		form.setTemplateName("登录验证码短信");
		form.setProviderTemplateCode("SMS_123456");
		form.setDescription("Dypns 登录验证码");
		form.setStatus(true);
		form.setPriority(5);
		form.setContent("您的验证码是${code}");

		sysSmsTemplateService.create(form);

		ArgumentCaptor<MessageTemplateEntity> entityCaptor = ArgumentCaptor.forClass(MessageTemplateEntity.class);
		verify(messageTemplateMapper).insert(entityCaptor.capture());
		MessageTemplateEntity saved = entityCaptor.getValue();

		assertThat(saved.getChannel()).isEqualTo(MessageChannel.SMS.name());
		assertThat(saved.getSceneCode()).isEqualTo("sms_login");
		assertThat(saved.getTemplateName()).isEqualTo("登录验证码短信");
		assertThat(saved.getProviderTemplateCode()).isEqualTo("SMS_123456");
		assertThat(saved.getDescription()).isEqualTo("Dypns 登录验证码");
		assertThat(saved.getStatus()).isTrue();
		assertThat(saved.getPriority()).isEqualTo(5);
		assertThat(saved.getBodyContent()).isEqualTo("您的验证码是${code}");
		assertThat(saved.getRequireFields()).isEqualTo("[]");
		assertThat(saved.getSubject()).isNull();
	}

	@Test
	@DisplayName("创建：content 为空时 bodyContent 落空串")
	void create_shouldDefaultEmptyBodyWhenContentNull() {
		when(messageTemplateMapper.insert(any(MessageTemplateEntity.class))).thenReturn(1);

		SmsTemplateForm form = new SmsTemplateForm();
		form.setTemplateCode("sms_reset");
		form.setTemplateName("重置密码短信");
		form.setProviderTemplateCode("SMS_654321");
		form.setStatus(false);
		form.setPriority(1);
		form.setContent(null);

		sysSmsTemplateService.create(form);

		ArgumentCaptor<MessageTemplateEntity> entityCaptor = ArgumentCaptor.forClass(MessageTemplateEntity.class);
		verify(messageTemplateMapper).insert(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getBodyContent()).isEmpty();
		assertThat(entityCaptor.getValue().getRequireFields()).isEqualTo("[]");
	}

}
