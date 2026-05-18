package com.auth.service.system.message.channel.dingtalk;

import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.modules.dingtalk.DingTalkChannelOptions;
import com.auth.module.message.modules.dingtalk.DingTalkMessageType;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.channel.dingtalk.model.RenderedDingTalkNotice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link DingTalkMessageSender} 单元测试
 */
@DisplayName("DingTalkMessageSender 钉钉发送")
@ExtendWith(MockitoExtension.class)
class DingTalkMessageSenderTest {

	@Mock
	private DingTalkTemplatePipeline dingTalkTemplatePipeline;

	@Mock
	private DingTalkWorkNoticeClient workNoticeClient;

	@Mock
	private ObjectProvider<DingTalkWorkNoticeClient> workNoticeClientProvider;

	private DingTalkMessageSender sender;

	@BeforeEach
	void setUp() {
		lenient().when(workNoticeClientProvider.getIfAvailable()).thenReturn(workNoticeClient);
		sender = new DingTalkMessageSender(dingTalkTemplatePipeline, workNoticeClientProvider);
	}

	@Test
	@DisplayName("模板模式：渲染后调用客户端发送")
	void sendByTemplate_templateMode_invokesClient() {
		when(dingTalkTemplatePipeline.render(eq("alert-job"), any()))
			.thenReturn(new RenderedDingTalkNotice(DingTalkMessageType.MARKDOWN, "标题", "正文内容"));

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.DING_TALK)
			.templateCode("alert-job")
			.targets(List.of("user001"))
			.variables(Map.of("name", "test"))
			.build();

		sender.sendByTemplate(command);

		verify(workNoticeClient).sendWorkNotice(List.of("user001"), DingTalkMessageType.MARKDOWN, "标题", "正文内容");
	}

	@Test
	@DisplayName("自定义正文：不查模板直接发送")
	void sendByTemplate_customBody_skipsPipeline() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.DING_TALK)
			.targets(List.of("user001"))
			.customBody(Boolean.TRUE)
			.body("hello")
			.options(DingTalkChannelOptions.builder().messageType(DingTalkMessageType.TEXT).build())
			.build();

		sender.sendByTemplate(command);

		verify(dingTalkTemplatePipeline, never()).render(any(), any());
		verify(workNoticeClient).sendWorkNotice(eq(List.of("user001")), eq(DingTalkMessageType.TEXT), isNull(),
				eq("hello"));
	}

	@Test
	@DisplayName("接收人为空：抛出业务异常")
	void sendByTemplate_emptyTargets_throws() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.DING_TALK)
			.templateCode("x")
			.targets(List.of())
			.build();

		assertThatThrownBy(() -> sender.sendByTemplate(command)).isInstanceOf(SystemBusinessException.class);
		verify(workNoticeClient, never()).sendWorkNotice(anyList(), any(), any(), any());
	}

}
