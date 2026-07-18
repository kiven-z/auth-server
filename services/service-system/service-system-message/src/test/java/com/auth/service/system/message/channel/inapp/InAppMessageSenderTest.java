package com.auth.service.system.message.channel.inapp;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.inapp.InAppChannelOptions;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.InAppMessageMapper;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.entity.InAppMessageRecipientEntity;
import com.auth.service.system.message.model.enums.DeliveryMode;
import com.auth.service.system.message.model.enums.InAppContentType;
import com.auth.service.system.message.model.enums.InAppMessageStatus;
import com.auth.service.system.message.model.enums.MessageSendSourceType;
import com.auth.service.system.message.model.value.inapp.RenderedInAppMessage;
import com.auth.service.system.message.service.InAppMessageRecipientWriteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.message.exception.MessageResultCode.IN_APP_TARGET_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link InAppMessageSender} 单元测试
 *
 * @author Bunny
 */
@DisplayName("InAppMessageSender 站内信发送")
@ExtendWith(MockitoExtension.class)
class InAppMessageSenderTest {

	@Mock
	private InAppTemplatePipeline inAppTemplatePipeline;

	@Mock
	private InAppMessageMapper inAppMessageMapper;

	@Mock
	private InAppMessageRecipientWriteService inAppMessageRecipientWriteService;

	@InjectMocks
	private InAppMessageSender sender;

	/**
	 * 模拟 task 主表插入分配 ID
	 * @param assignedId 分配 ID
	 */
	private void stubInsertTaskId(long assignedId) {
		doAnswer(invocation -> {
			invocation.getArgument(0, InAppMessageEntity.class).setId(assignedId);
			return 1;
		}).when(inAppMessageMapper).insert(any(InAppMessageEntity.class));
	}

	@Test
	@DisplayName("渠道标识为 IN_APP")
	void channel_shouldBeInApp() {
		assertThat(sender.channel()).isEqualTo(MessageChannel.IN_APP);
	}

	@Test
	@DisplayName("模板模式：先写 task 主表，再展开去重 userId 写 inbox，收尾标记 SUCCESS")
	void sendByTemplate_shouldPersistTaskAndExpandInbox() {
		when(inAppTemplatePipeline.render("notice", Map.of("name", "Bunny"))).thenReturn(RenderedInAppMessage.builder()
			.title("Hello Bunny")
			.content("Body Bunny")
			.contentType(InAppContentType.TEXT)
			.sceneCode("notice")
			.build());
		stubInsertTaskId(9001L);
		when(inAppMessageRecipientWriteService.insertBatch(any())).thenAnswer(invocation -> {
			List<?> rows = invocation.getArgument(0);
			return rows.size();
		});

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.IN_APP)
			.templateCode("notice")
			.targets(List.of("1001", "1002", "1001"))
			.variables(Map.of("name", "Bunny"))
			.options(InAppChannelOptions.builder().bizType("NOTICE").linkUrl("/home").build())
			.build();

		sender.sendByTemplate(command);

		ArgumentCaptor<InAppMessageEntity> taskCaptor = ArgumentCaptor.forClass(InAppMessageEntity.class);
		verify(inAppMessageMapper).insert(taskCaptor.capture());
		InAppMessageEntity task = taskCaptor.getValue();
		assertThat(task.getSourceType()).isEqualTo(MessageSendSourceType.TEMPLATE.name());
		assertThat(task.getDeliveryMode()).isEqualTo(DeliveryMode.PUSH.name());
		assertThat(task.getTitle()).isEqualTo("Hello Bunny");
		assertThat(task.getContent()).isEqualTo("Body Bunny");
		assertThat(task.getContentType()).isEqualTo(InAppContentType.TEXT.name());
		assertThat(task.getSceneCode()).isEqualTo("notice");
		assertThat(task.getBizType()).isEqualTo("NOTICE");
		assertThat(task.getLinkUrl()).isEqualTo("/home");
		assertThat(task.getTotalCount()).isEqualTo(2);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<InAppMessageRecipientEntity>> captor = ArgumentCaptor.forClass(List.class);
		verify(inAppMessageRecipientWriteService).insertBatch(captor.capture());
		List<InAppMessageRecipientEntity> rows = captor.getValue();
		assertThat(rows).hasSize(2)
			.allMatch(r -> r.getMessageId() == 9001L)
			.allMatch(r -> Boolean.FALSE.equals(r.getIsRead()))
			.allMatch(r -> Boolean.FALSE.equals(r.getIsDeleted()));
		assertThat(rows).extracting(InAppMessageRecipientEntity::getUserId).containsExactly(1001L, 1002L);

		verify(inAppMessageMapper).finishTask(9001L, InAppMessageStatus.SUCCESS.name(), 2);
	}

	@Test
	@DisplayName("自定义正文：源类型 SYSTEM，正文/标题走 options")
	void sendByTemplate_shouldSupportCustomBody() {
		stubInsertTaskId(88L);
		when(inAppMessageRecipientWriteService.insertBatch(any())).thenReturn(1);

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.IN_APP)
			.targets(List.of("88"))
			.customBody(Boolean.TRUE)
			.body("manual body")
			.options(InAppChannelOptions.builder().title("manual title").contentType("MARKDOWN").build())
			.build();

		sender.sendByTemplate(command);

		ArgumentCaptor<InAppMessageEntity> taskCaptor = ArgumentCaptor.forClass(InAppMessageEntity.class);
		verify(inAppMessageMapper).insert(taskCaptor.capture());
		InAppMessageEntity task = taskCaptor.getValue();
		assertThat(task.getSourceType()).isEqualTo(MessageSendSourceType.SYSTEM.name());
		assertThat(task.getTitle()).isEqualTo("manual title");
		assertThat(task.getContent()).isEqualTo("manual body");
		assertThat(task.getContentType()).isEqualTo(InAppContentType.MARKDOWN.name());
	}

	@Test
	@DisplayName("自定义正文缺标题：抛出 PARAM_REQUIRED")
	void sendByTemplate_shouldThrowWhenCustomBodyWithoutTitle() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.IN_APP)
			.targets(List.of("88"))
			.customBody(Boolean.TRUE)
			.body("manual body")
			.build();

		assertThatThrownBy(() -> sender.sendByTemplate(command)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(PARAM_REQUIRED);
	}

	@Test
	@DisplayName("targets 非数字：抛出 IN_APP_TARGET_INVALID")
	void sendByTemplate_shouldThrowWhenTargetNotUserId() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.IN_APP)
			.templateCode("notice")
			.targets(List.of("not-a-number"))
			.variables(Map.of())
			.build();

		assertThatThrownBy(() -> sender.sendByTemplate(command)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_TARGET_INVALID);
	}

	@Test
	@DisplayName("targets 含空白：跳过空白后写入有效用户")
	void sendByTemplate_shouldSkipBlankTargets() {
		when(inAppTemplatePipeline.render("notice", Map.of())).thenReturn(RenderedInAppMessage.builder()
			.title("t")
			.content("c")
			.contentType(InAppContentType.TEXT)
			.sceneCode("notice")
			.build());
		stubInsertTaskId(500L);
		when(inAppMessageRecipientWriteService.insertBatch(any())).thenAnswer(invocation -> {
			List<?> rows = invocation.getArgument(0);
			return rows.size();
		});

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.IN_APP)
			.templateCode("notice")
			.targets(List.of("1001", " ", "", "1002"))
			.variables(Map.of())
			.build();

		sender.sendByTemplate(command);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<InAppMessageRecipientEntity>> captor = ArgumentCaptor.forClass(List.class);
		verify(inAppMessageRecipientWriteService).insertBatch(captor.capture());
		assertThat(captor.getValue()).extracting(InAppMessageRecipientEntity::getUserId).containsExactly(1001L, 1002L);
		verify(inAppMessageMapper).finishTask(500L, InAppMessageStatus.SUCCESS.name(), 2);
	}

}
