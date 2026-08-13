package com.auth.service.system.file.export.notify;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.enums.scene.InAppMessageScene;
import com.auth.module.message.api.port.MessageSendOperations;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 文件异步导出终态站内信通知
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class FileExportTaskNotifier {

	private static final int ERROR_SUMMARY_MAX_LENGTH = 200;

	private static final DateTimeFormatter UTC_DISPLAY = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
		.withZone(ZoneOffset.UTC);

	private final MessageSendOperations messageSendOperations;

	private static String resolveTaskName(FileExportTaskEntity task) {
		return CharSequenceUtil.blankToDefault(task.getBizType(), "EXPORT");
	}

	private static String formatInstant(Instant instant) {
		Instant value = Objects.requireNonNullElse(instant, Instant.now());
		return UTC_DISPLAY.format(value) + " UTC";
	}

	private static String truncateErrorSummary(String message) {
		if (CharSequenceUtil.isBlank(message)) {
			return "导出失败";
		}
		return CharSequenceUtil.maxLength(message.trim(), ERROR_SUMMARY_MAX_LENGTH);
	}

	/**
	 * 导出成功通知
	 * @param task 任务实体
	 * @param fileName 产物文件名
	 */
	public void notifySuccess(FileExportTaskEntity task, String fileName) {
		Long userId = task.getCreatedBy();
		if (userId == null) {
			log.warn("Skip export success notification: taskId={}, createdBy is null", task.getId());
			return;
		}
		Map<String, Object> variables = new HashMap<>(4);
		variables.put("taskId", String.valueOf(task.getId()));
		variables.put("taskName", resolveTaskName(task));
		variables.put("fileName", CharSequenceUtil.blankToDefault(fileName, "-"));
		variables.put("finishedAt", formatInstant(task.getFinishedAt()));
		sendQuietly(InAppMessageScene.FILE_EXPORT_SUCCESS, userId, variables, task.getId());
	}

	/**
	 * 导出终态失败通知
	 * @param task 任务实体
	 * @param errorSummary 失败摘要
	 */
	public void notifyTerminalFailure(FileExportTaskEntity task, String errorSummary) {
		Long userId = task.getCreatedBy();
		if (userId == null) {
			log.warn("Skip export failure notification: taskId={}, createdBy is null", task.getId());
			return;
		}
		Map<String, Object> variables = new HashMap<>(4);
		variables.put("taskId", String.valueOf(task.getId()));
		variables.put("taskName", resolveTaskName(task));
		variables.put("failedAt", formatInstant(task.getFinishedAt()));
		String truncateErrorSummary = truncateErrorSummary(
				CharSequenceUtil.blankToDefault(task.getErrorMessage(), errorSummary));
		variables.put("errorSummary", truncateErrorSummary);
		sendQuietly(InAppMessageScene.FILE_EXPORT_FAILED, userId, variables, task.getId());
	}

	private void sendQuietly(InAppMessageScene scene, Long userId, Map<String, Object> variables, Long taskId) {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(scene.getChannel())
			.templateCode(scene.getTemplateCode())
			.targets(List.of(String.valueOf(userId)))
			.variables(variables)
			.build();
		try {
			messageSendOperations.sendByTemplate(command);
		}
		catch (Exception exception) {
			log.warn("Failed to send export task notification: taskId={}, scene={}", taskId, scene.getTemplateCode(),
					exception);
		}
	}

}
