package com.auth.service.system.file.export.notify;

import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.enums.scene.InAppMessageScene;
import com.auth.module.message.api.port.MessageSendOperations;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * {@link FileExportTaskNotifier} 单元测试
 */
@DisplayName("FileExportTaskNotifier 导出终态通知")
@ExtendWith(MockitoExtension.class)
class FileExportTaskNotifierTest {

	@Mock
	private MessageSendOperations messageSendOperations;

	@InjectMocks
	private FileExportTaskNotifier notifier;

	@Test
	@DisplayName("notifySuccess：只传场景、接收人与变量")
	void notifySuccess_shouldSendSuccessTemplate() {
		FileExportTaskEntity task = newTask();
		task.setFinishedAt(Instant.parse("2026-08-08T15:30:00Z"));

		notifier.notifySuccess(task, "user-list.xlsx");

		ArgumentCaptor<TemplateMessageCommand> captor = ArgumentCaptor.forClass(TemplateMessageCommand.class);
		verify(messageSendOperations).sendByTemplate(captor.capture());
		TemplateMessageCommand command = captor.getValue();
		assertThat(command.getChannel()).isEqualTo(InAppMessageScene.FILE_EXPORT_SUCCESS.getChannel());
		assertThat(command.getTemplateCode()).isEqualTo(InAppMessageScene.FILE_EXPORT_SUCCESS.getTemplateCode());
		assertThat(command.getTargets()).containsExactly("9001");
		assertThat(command.getOptions()).isNull();
		Map<String, Object> variables = command.getVariables();
		assertThat(variables.get("taskId")).isEqualTo("101");
		assertThat(variables.get("taskName")).isEqualTo("SYS_USER");
		assertThat(variables.get("fileName")).isEqualTo("user-list.xlsx");
		assertThat(variables.get("finishedAt")).isEqualTo("2026-08-08 15:30:00 UTC");
	}

	@Test
	@DisplayName("notifyTerminalFailure：发送失败模板并截断错误摘要")
	void notifyTerminalFailure_shouldSendFailedTemplate() {
		FileExportTaskEntity task = newTask();
		task.setFinishedAt(Instant.parse("2026-08-08T15:31:00Z"));
		task.setErrorMessage("导出数据源超时");

		notifier.notifyTerminalFailure(task, "fallback");

		ArgumentCaptor<TemplateMessageCommand> captor = ArgumentCaptor.forClass(TemplateMessageCommand.class);
		verify(messageSendOperations).sendByTemplate(captor.capture());
		TemplateMessageCommand command = captor.getValue();
		assertThat(command.getChannel()).isEqualTo(InAppMessageScene.FILE_EXPORT_FAILED.getChannel());
		assertThat(command.getTemplateCode()).isEqualTo(InAppMessageScene.FILE_EXPORT_FAILED.getTemplateCode());
		assertThat(command.getTargets()).containsExactly("9001");
		assertThat(command.getOptions()).isNull();
		Map<String, Object> variables = command.getVariables();
		assertThat(variables.get("taskId")).isEqualTo("101");
		assertThat(variables.get("taskName")).isEqualTo("SYS_USER");
		assertThat(variables.get("failedAt")).isEqualTo("2026-08-08 15:31:00 UTC");
		assertThat(variables.get("errorSummary")).isEqualTo("导出数据源超时");
	}

	@Test
	@DisplayName("createdBy 为空：不发送")
	void notifySuccess_shouldSkipWhenCreatedByNull() {
		FileExportTaskEntity task = newTask();
		task.setCreatedBy(null);

		notifier.notifySuccess(task, "user-list.xlsx");

		verify(messageSendOperations, never()).sendByTemplate(any());
	}

	@Test
	@DisplayName("发送异常：不向外抛出")
	void sendQuietly_shouldSwallowException() {
		doThrow(new RuntimeException("send failed")).when(messageSendOperations).sendByTemplate(any());

		notifier.notifySuccess(newTask(), "user-list.xlsx");

		verify(messageSendOperations).sendByTemplate(any());
	}

	private FileExportTaskEntity newTask() {
		FileExportTaskEntity task = new FileExportTaskEntity();
		task.setId(101L);
		task.setBizType("SYS_USER");
		task.setCreatedBy(9001L);
		return task;
	}

}
