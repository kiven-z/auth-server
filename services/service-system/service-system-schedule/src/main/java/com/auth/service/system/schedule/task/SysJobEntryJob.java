package com.auth.service.system.schedule.task;

import com.auth.service.system.schedule.support.task.QuartzJobSupport;
import com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationContext;

/**
 * 统一定时任务 Quartz 入口：按 {@code taskType} 委派 {@link SysJobTaskExecutor}
 *
 * @author Bunny
 */
@Slf4j
public class SysJobEntryJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			ApplicationContext app = QuartzJobSupport.getApplicationContext(context);
			JobDataMap jobDataMap = context.getMergedJobDataMap();
			SysJobTaskType taskType = SysJobTaskType.resolve(jobDataMap.getString(SysJobQuartzDataKeys.TASK_TYPE));
			SysJobTaskExecutorRegistry registry = app.getBean(SysJobTaskExecutorRegistry.class);
			registry.resolve(taskType).execute(context);
		}
		catch (JobExecutionException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new JobExecutionException(exception);
		}
	}

	/**
	 * 禁止并发版本：仅通过注解控制并发
	 */
	@DisallowConcurrentExecution
	public static class Disallow extends SysJobEntryJob {

	}

}
