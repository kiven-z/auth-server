package com.auth.service.system.message.schedule.executor;

import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.message.schedule.support.MessageDispatchCommandParser;
import com.auth.service.system.message.service.MessageDispatchService;
import com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.task.executor.SysJobTaskExecutor;
import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

/**
 * 消息派发任务执行器
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class MessageDispatchTaskExecutor implements SysJobTaskExecutor {

	private final MessageDispatchCommandParser messageDispatchCommandParser;

	private final MessageDispatchService messageDispatchService;

	@Override
	public SysJobTaskType taskType() {
		return SysJobTaskType.MESSAGE_DISPATCH;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			String payloadJson = context.getMergedJobDataMap().getString(SysJobQuartzDataKeys.PAYLOAD_JSON);
			TemplateMessageCommand command = messageDispatchCommandParser.parseAndValidate(payloadJson);
			messageDispatchService.sendByTemplate(command);
		}
		catch (Exception exception) {
			throw new JobExecutionException(exception);
		}
	}

}
