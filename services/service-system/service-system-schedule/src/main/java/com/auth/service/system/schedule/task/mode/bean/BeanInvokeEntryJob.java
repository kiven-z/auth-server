package com.auth.service.system.schedule.task.mode.bean;

import com.auth.service.system.schedule.model.constans.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.support.QuartzBeanInvoker;
import com.auth.service.system.schedule.task.QuartzJobSupport;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationContext;

/**
 * Bean 调用模式入口 Job：从 JobDataMap 读取 invoke_target 并委派给 {@link QuartzBeanInvoker}
 *
 * @author Bunny
 */
@Slf4j
public class BeanInvokeEntryJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			// 获取Spring上下文
			ApplicationContext app = QuartzJobSupport.getApplicationContext(context);

			// 获取调用目标
			String invokeTarget = context.getMergedJobDataMap().getString(SysJobQuartzDataKeys.INVOKE_TARGET);

			// 获取QuartzBeanInvoker实例
			QuartzBeanInvoker invoker = app.getBean(QuartzBeanInvoker.class);

			// 调用QuartzBeanInvoker实例
			String out = invoker.invoke(invokeTarget);
			// 如果日志级别为DEBUG，则打印日志
			JobDetail jobDetail = context.getJobDetail();
			log.debug("Quartz bean job ok jobKey={} result={}", jobDetail.getKey(), out);
		}
		catch (JobExecutionException e) {
			throw e;
		}
		catch (Exception e) {
			throw new JobExecutionException(e);
		}
	}

	/**
	 * 禁止并发版本：仅通过注解控制并发
	 */
	@DisallowConcurrentExecution
	public static class Disallow extends BeanInvokeEntryJob {

	}

}
