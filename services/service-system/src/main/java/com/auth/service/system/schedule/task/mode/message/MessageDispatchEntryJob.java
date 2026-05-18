package com.auth.service.system.schedule.task.mode.message;

import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.service.system.message.api.MessageDispatchService;
import com.auth.service.system.schedule.model.constans.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.support.MessageDispatchCommandParser;
import com.auth.service.system.schedule.task.mode.QuartzJobSupport;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

/**
 * 统一消息派发定时入口（调度层不耦合具体消息渠道）
 *
 * @author Bunny
 */
public class MessageDispatchEntryJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			ApplicationContext app = QuartzJobSupport.getApplicationContext(context);
			JobDataMap jobDataMap = context.getMergedJobDataMap();
			String payloadJson = jobDataMap.getString(SysJobQuartzDataKeys.PAYLOAD_JSON);

			MessageDispatchCommandParser parser = app.getBean(MessageDispatchCommandParser.class);
			TemplateMessageCommand command = parser.parseAndValidate(payloadJson);
			MessageDispatchService dispatchService = app.getBean(MessageDispatchService.class);
			dispatchService.sendByTemplate(command);
		}
		catch (JobExecutionException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new JobExecutionException(exception);
		}
	}

	/**
	 * 禁止并发版本
	 */
	@DisallowConcurrentExecution
	public static class Disallow extends MessageDispatchEntryJob {

	}

}
