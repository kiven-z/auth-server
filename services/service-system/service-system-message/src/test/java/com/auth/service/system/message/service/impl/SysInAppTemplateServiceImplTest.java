package com.auth.service.system.message.service.impl;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.MessageTemplateMapper;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.form.inapp.InAppTemplateForm;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link SysInAppTemplateServiceImpl} 站内信模板创建与更新单元测试
 */
@DisplayName("SysInAppTemplateServiceImpl 站内信模板服务")
@ExtendWith(MockitoExtension.class)
class SysInAppTemplateServiceImplTest {

	@Mock
	private MessageTemplateMapper messageTemplateMapper;

	@InjectMocks
	private SysInAppTemplateServiceImpl sysInAppTemplateService;

	@BeforeAll
	static void initMybatisPlusTableInfo() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
				MessageTemplateEntity.class);
	}

	@BeforeEach
	void setUp() throws Exception {
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysInAppTemplateService, messageTemplateMapper);
	}

	@Test
	@DisplayName("创建：写入 IN_APP 渠道、标题与 TEXT 正文，并初始化空 require_fields")
	void create_shouldPersistInAppChannelAndTextBody() {
		when(messageTemplateMapper.insert(any(MessageTemplateEntity.class))).thenReturn(1);

		InAppTemplateForm form = new InAppTemplateForm();
		form.setTemplateCode("in_app_notice");
		form.setTemplateName("系统通知");
		form.setSubject("通知：${title}");
		form.setContentType("TEXT");
		form.setContent("你好，${userName}");
		form.setDescription("站内通知模板");
		form.setStatus(true);
		form.setPriority(5);

		sysInAppTemplateService.create(form);

		ArgumentCaptor<MessageTemplateEntity> entityCaptor = ArgumentCaptor.forClass(MessageTemplateEntity.class);
		verify(messageTemplateMapper).insert(entityCaptor.capture());
		MessageTemplateEntity saved = entityCaptor.getValue();

		assertThat(saved.getChannel()).isEqualTo(MessageChannel.IN_APP.name());
		assertThat(saved.getSceneCode()).isEqualTo("in_app_notice");
		assertThat(saved.getTemplateName()).isEqualTo("系统通知");
		assertThat(saved.getSubject()).isEqualTo("通知：${title}");
		assertThat(saved.getImMessageType()).isEqualTo("TEXT");
		assertThat(saved.getBodyContent()).isEqualTo("你好，${userName}");
		assertThat(saved.getDescription()).isEqualTo("站内通知模板");
		assertThat(saved.getStatus()).isTrue();
		assertThat(saved.getPriority()).isEqualTo(5);
		assertThat(saved.getRequireFields()).isEqualTo("[]");
	}

	@Test
	@DisplayName("创建：MARKDOWN 正文原样落库")
	void create_shouldPersistMarkdownBodyAsIs() {
		when(messageTemplateMapper.insert(any(MessageTemplateEntity.class))).thenReturn(1);

		InAppTemplateForm form = new InAppTemplateForm();
		form.setTemplateCode("in_app_md");
		form.setTemplateName("Markdown 通知");
		form.setSubject("MD 标题");
		form.setContentType("MARKDOWN");
		form.setContent("**加粗**与[链接](https://example.com)");
		form.setStatus(true);
		form.setPriority(3);

		sysInAppTemplateService.create(form);

		ArgumentCaptor<MessageTemplateEntity> entityCaptor = ArgumentCaptor.forClass(MessageTemplateEntity.class);
		verify(messageTemplateMapper).insert(entityCaptor.capture());
		MessageTemplateEntity saved = entityCaptor.getValue();

		assertThat(saved.getImMessageType()).isEqualTo("MARKDOWN");
		assertThat(saved.getBodyContent()).isEqualTo("**加粗**与[链接](https://example.com)");
	}

	@Test
	@DisplayName("创建：未知 contentType 按 TEXT，正文原样落库")
	void create_shouldFallbackUnknownContentTypeToText() {
		when(messageTemplateMapper.insert(any(MessageTemplateEntity.class))).thenReturn(1);

		InAppTemplateForm form = new InAppTemplateForm();
		form.setTemplateCode("in_app_unknown");
		form.setTemplateName("未知类型");
		form.setSubject("标题");
		form.setContentType("UNKNOWN");
		form.setContent("正文");
		form.setStatus(false);
		form.setPriority(1);

		sysInAppTemplateService.create(form);

		ArgumentCaptor<MessageTemplateEntity> entityCaptor = ArgumentCaptor.forClass(MessageTemplateEntity.class);
		verify(messageTemplateMapper).insert(entityCaptor.capture());
		MessageTemplateEntity saved = entityCaptor.getValue();

		assertThat(saved.getImMessageType()).isEqualTo("TEXT");
		assertThat(saved.getBodyContent()).isEqualTo("正文");
	}

	@Test
	@DisplayName("更新：覆盖元数据与正文类型，保留 require_fields 与 channel")
	void update_shouldOverwriteFieldsAndKeepRequireFields() {
		// 已有站内信模板，含变量声明
		MessageTemplateEntity exists = new MessageTemplateEntity();
		exists.setId(20L);
		exists.setChannel(MessageChannel.IN_APP.name());
		exists.setSceneCode("old_code");
		exists.setTemplateName("旧名称");
		exists.setSubject("旧标题");
		exists.setImMessageType("TEXT");
		exists.setBodyContent("旧正文");
		exists.setDescription("旧描述");
		exists.setStatus(false);
		exists.setPriority(1);
		exists.setRequireFields("[{\"key\":\"userName\",\"exampleValue\":\"张三\"}]");
		when(messageTemplateMapper.selectById(20L)).thenReturn(exists);
		when(messageTemplateMapper.updateById(any(MessageTemplateEntity.class))).thenReturn(1);

		InAppTemplateForm form = new InAppTemplateForm();
		form.setId(20L);
		form.setTemplateCode("in_app_notice");
		form.setTemplateName("系统通知");
		form.setSubject("通知：${title}");
		form.setContentType("MARKDOWN");
		form.setContent("**新正文** ${userName}");
		form.setDescription("新描述");
		form.setStatus(true);
		form.setPriority(8);

		sysInAppTemplateService.update(form);

		ArgumentCaptor<MessageTemplateEntity> entityCaptor = ArgumentCaptor.forClass(MessageTemplateEntity.class);
		verify(messageTemplateMapper).updateById(entityCaptor.capture());
		MessageTemplateEntity updated = entityCaptor.getValue();

		assertThat(updated.getChannel()).isEqualTo(MessageChannel.IN_APP.name());
		assertThat(updated.getSceneCode()).isEqualTo("in_app_notice");
		assertThat(updated.getTemplateName()).isEqualTo("系统通知");
		assertThat(updated.getSubject()).isEqualTo("通知：${title}");
		assertThat(updated.getImMessageType()).isEqualTo("MARKDOWN");
		assertThat(updated.getBodyContent()).isEqualTo("**新正文** ${userName}");
		assertThat(updated.getDescription()).isEqualTo("新描述");
		assertThat(updated.getStatus()).isTrue();
		assertThat(updated.getPriority()).isEqualTo(8);
		assertThat(updated.getRequireFields()).isEqualTo("[{\"key\":\"userName\",\"exampleValue\":\"张三\"}]");
	}

	@Test
	@DisplayName("更新：记录不存在时抛出 DATA_NOT_EXIST")
	void update_shouldThrowWhenMissing() {
		when(messageTemplateMapper.selectById(99L)).thenReturn(null);

		InAppTemplateForm form = new InAppTemplateForm();
		form.setId(99L);
		form.setTemplateCode("in_app_missing");
		form.setTemplateName("不存在");
		form.setSubject("标题");
		form.setContentType("TEXT");
		form.setContent("正文");
		form.setStatus(true);
		form.setPriority(1);

		assertThatThrownBy(() -> sysInAppTemplateService.update(form)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
		verify(messageTemplateMapper, never()).updateById(any(MessageTemplateEntity.class));
	}

	@Test
	@DisplayName("更新：不校验渠道，仅按 id 更新且不改写 channel")
	void update_shouldNotValidateChannel() {
		MessageTemplateEntity exists = new MessageTemplateEntity();
		exists.setId(22L);
		exists.setChannel(MessageChannel.EMAIL.name());
		exists.setRequireFields("[]");
		when(messageTemplateMapper.selectById(22L)).thenReturn(exists);
		when(messageTemplateMapper.updateById(any(MessageTemplateEntity.class))).thenReturn(1);

		InAppTemplateForm form = new InAppTemplateForm();
		form.setId(22L);
		form.setTemplateCode("in_app_any_channel");
		form.setTemplateName("不校验渠道");
		form.setSubject("标题");
		form.setContentType("TEXT");
		form.setContent("正文");
		form.setStatus(true);
		form.setPriority(1);

		sysInAppTemplateService.update(form);

		ArgumentCaptor<MessageTemplateEntity> entityCaptor = ArgumentCaptor.forClass(MessageTemplateEntity.class);
		verify(messageTemplateMapper).updateById(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getChannel()).isEqualTo(MessageChannel.EMAIL.name());
		assertThat(entityCaptor.getValue().getRequireFields()).isEqualTo("[]");
	}

}
