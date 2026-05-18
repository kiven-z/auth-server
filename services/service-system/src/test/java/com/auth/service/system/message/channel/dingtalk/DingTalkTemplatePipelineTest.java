package com.auth.service.system.message.channel.dingtalk;

import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.config.properties.DingTalkProperties;
import com.auth.module.message.modules.dingtalk.DingTalkMessageType;
import com.auth.service.system.admin.mapper.MessageTemplateMapper;
import com.auth.service.system.admin.model.entity.MessageTemplateEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.channel.dingtalk.model.RenderedDingTalkNotice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link DingTalkTemplatePipeline} 单元测试
 */
@DisplayName("DingTalkTemplatePipeline 钉钉模板管道")
@ExtendWith(MockitoExtension.class)
class DingTalkTemplatePipelineTest {

	@Mock
	private MessageTemplateMapper messageTemplateMapper;

	private DingTalkTemplatePipeline pipeline;

	@BeforeEach
	void setUp() {
		DingTalkProperties properties = new DingTalkProperties();
		properties.setDefaultMessageType(DingTalkMessageType.TEXT);
		pipeline = new DingTalkTemplatePipeline(messageTemplateMapper, properties);
	}

	@Test
	@DisplayName("渲染成功：返回消息类型与正文")
	void render_success() {
		MessageTemplateEntity entity = new MessageTemplateEntity();
		entity.setTemplateName("job-alert");
		entity.setImMessageType("MARKDOWN");
		entity.setSubject("告警 ${name}");
		entity.setBodyContent("内容 ${name}");
		entity.setRequireFields("[]");
		when(messageTemplateMapper.selectEnabledBySceneCodeAndChannel(eq("job-alert"),
				eq(MessageChannel.DING_TALK.name())))
			.thenReturn(entity);

		RenderedDingTalkNotice rendered = pipeline.render("job-alert", Map.of("name", "disk"));

		assertThat(rendered.messageType()).isEqualTo(DingTalkMessageType.MARKDOWN);
		assertThat(rendered.title()).contains("disk");
		assertThat(rendered.content()).contains("disk");
	}

	@Test
	@DisplayName("模板不存在：抛出业务异常")
	void render_templateNotFound_throws() {
		when(messageTemplateMapper.selectEnabledBySceneCodeAndChannel(eq("missing"),
				eq(MessageChannel.DING_TALK.name())))
			.thenReturn(null);

		assertThatThrownBy(() -> pipeline.render("missing", Map.of())).isInstanceOf(SystemBusinessException.class);
	}

}
