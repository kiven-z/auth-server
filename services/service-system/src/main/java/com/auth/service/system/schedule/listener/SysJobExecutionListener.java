package com.auth.service.system.schedule.listener;

import com.auth.service.system.schedule.config.ScheduleJobProperties;
import com.auth.service.system.schedule.model.constans.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.service.SysJobLogService;
import com.auth.service.system.schedule.support.SysJobCircuitBreaker;
import org.quartz.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 统一记录 {@code log_job}，并在失败时触发熔断判断
 *
 * @author Bunny
 */
@Component
public class SysJobExecutionListener implements JobListener {

	/**
	 * 开始时间
	 */
	private static final ThreadLocal<Long> START_MS = new ThreadLocal<>();

	/**
	 * 系统任务日志服务
	 */
	private final SysJobLogService sysJobLogService;

	/**
	 * 系统任务熔断器
	 */
	private final SysJobCircuitBreaker sysJobCircuitBreaker;

	private final ScheduleJobProperties scheduleJobProperties;

	/**
	 * @param sysJobLogService 任务日志服务
	 * @param sysJobCircuitBreaker 熔断器（延迟注入，避免参与 Quartz Scheduler 创建期的循环依赖）
	 * @param scheduleJobProperties 任务配置
	 */
	public SysJobExecutionListener(SysJobLogService sysJobLogService, @Lazy SysJobCircuitBreaker sysJobCircuitBreaker,
			ScheduleJobProperties scheduleJobProperties) {
		this.sysJobLogService = sysJobLogService;
		this.sysJobCircuitBreaker = sysJobCircuitBreaker;
		this.scheduleJobProperties = scheduleJobProperties;
	}

	/**
	 * 获取监听器名称
	 * @return 监听器名称
	 */
	@Override
	public String getName() {
		return "sysJobExecutionListener";
	}

	/**
	 * 任务执行前
	 * @param context 任务执行上下文
	 */
	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		START_MS.set(System.currentTimeMillis());
	}

	/**
	 * 任务执行被拒绝
	 * @param context 任务执行上下文
	 */
	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
		START_MS.remove();
	}

	/**
	 * 任务执行后
	 * @param context 任务执行上下文
	 * @param jobException 任务执行异常
	 */
	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		try {
			// 计算任务执行时间
			long currentTimeMillis = System.currentTimeMillis();
			long elapsed = currentTimeMillis - (START_MS.get() == null ? currentTimeMillis : START_MS.get());

			// 获取任务名称 和 获取任务分组
			JobKey jobKey = context.getJobDetail().getKey();
			String jobName = jobKey.getName();
			String jobGroup = jobKey.getGroup();

			// 获取任务执行目标 和 获取任务ID
			JobDataMap jobDataMap = context.getMergedJobDataMap();
			String invokeTarget = jobDataMap.getString(SysJobQuartzDataKeys.INVOKE_TARGET);
			String sysJobIdStr = jobDataMap.getString(SysJobQuartzDataKeys.SYS_JOB_ID);
			String triggerType = jobDataMap.getString(SysJobQuartzDataKeys.TRIGGER_TYPE);
			if (triggerType == null || triggerType.isBlank()) {
				triggerType = SysJobQuartzDataKeys.TRIGGER_TYPE_SCHEDULE;
			}

			// 转换任务ID
			Long jobId = null;
			if (sysJobIdStr != null) {
				jobId = Long.parseLong(sysJobIdStr);
			}

			// 是否记录成功的日志
			boolean hasRecordSuccessLogEnable = scheduleJobProperties.isSuccessEnabled();
			if (jobException == null && hasRecordSuccessLogEnable) {
				sysJobLogService.recordSuccess(jobId, jobName, jobGroup, invokeTarget, triggerType, elapsed, "OK");
			}

			// 记录失败日志
			if (jobException != null) {
				sysJobLogService.recordFailure(jobId, jobName, jobGroup, invokeTarget, triggerType, elapsed,
						jobException);
				// 触发熔断判断
				sysJobCircuitBreaker.evaluateAfterFailure(jobId, jobName, jobGroup);
			}
		}
		finally {
			START_MS.remove();
		}
	}

}
