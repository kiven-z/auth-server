package com.auth.service.system.schedule.task;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.auth.service.system.schedule.model.enums.SysJobTaskType;

/**
 * 定时任务类型执行 SPI（调度层通过 {@link SysJobEntryJob} 委派）
 *
 * @author Bunny
 */
public interface SysJobTaskExecutor {

	/**
	 * 支持的任务类型
	 * @return 任务类型
	 */
	SysJobTaskType taskType();

	/**
	 * 执行任务
	 * @param context Quartz 上下文
	 * @throws JobExecutionException 任务执行异常
	 */
	void execute(JobExecutionContext context) throws JobExecutionException;

}
