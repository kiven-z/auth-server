package com.auth.service.system.schedule.task.mode.custom;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.model.constans.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.task.QuartzJobSupport;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationContext;

/**
 * 自定义 Job 模式入口：从 {@code JobDataMap.jobClass} 读取目标 Job 类并委派执行
 *
 * @author Bunny
 */
@Slf4j
public class DelegatingCustomEntryJob implements Job {

	@Override
	@SuppressWarnings("unchecked")
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// 获取任务数据
		JobDataMap jobDataMap = context.getMergedJobDataMap();
		// 获取任务类名
		String jobClassName = jobDataMap.getString(SysJobQuartzDataKeys.JOB_CLASS);
		// 如果任务类名为空，则抛出异常
		if (CharSequenceUtil.isBlank(jobClassName)) {
			throw new JobExecutionException("job_class 为空");
		}

		try {
			// 获取Spring上下文
			ApplicationContext app = QuartzJobSupport.getApplicationContext(context);

			// 获取任务类
			Class<?> raw = Class.forName(jobClassName);
			// 如果任务类不是Job的子类，则抛出异常
			if (!Job.class.isAssignableFrom(raw)) {
				throw new JobExecutionException("job_class 未实现 org.quartz.Job: " + jobClassName);
			}

			// 获取任务类
			Class<? extends Job> target = (Class<? extends Job>) raw;
			// 创建任务实例
			Job job = app.getAutowireCapableBeanFactory().createBean(target);
			// 执行任务
			job.execute(context);
		}
		catch (JobExecutionException e) {
			throw e;
		}
		catch (Exception e) {
			log.warn("自定义 Job 执行失败 jobClass={} jobKey={}", jobClassName, context.getJobDetail().getKey(), e);
			throw new JobExecutionException(e);
		}
	}

	/**
	 * 禁止并发版本：仅通过注解控制并发
	 */
	@DisallowConcurrentExecution
	public static class Disallow extends DelegatingCustomEntryJob {

	}

}
