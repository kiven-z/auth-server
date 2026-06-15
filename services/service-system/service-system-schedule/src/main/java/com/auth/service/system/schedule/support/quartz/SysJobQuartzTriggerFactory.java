package com.auth.service.system.schedule.support.quartz;

import com.auth.service.system.schedule.model.entity.SysJobEntity;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

import static com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys.TRIGGER_SUFFIX;

/**
 * 根据 {@link SysJobEntity} 构建 Quartz {@link CronTrigger}
 *
 * @author Bunny
 */
@Component
public class SysJobQuartzTriggerFactory {

	private static CronScheduleBuilder applyMisfire(CronScheduleBuilder cron, Integer policy) {
		int resolvedPolicy = Objects.requireNonNullElse(policy, 2);
		return switch (resolvedPolicy) {
			case 1 -> cron.withMisfireHandlingInstructionIgnoreMisfires();
			case 2 -> cron.withMisfireHandlingInstructionFireAndProceed();
			case 3 -> cron.withMisfireHandlingInstructionDoNothing();
			default -> cron.withMisfireHandlingInstructionFireAndProceed();
		};
	}

	/**
	 * 构建 CronTrigger
	 * @param job 任务实体
	 * @param jobKey 任务键
	 * @return CronTrigger
	 */
	public CronTrigger build(SysJobEntity job, JobKey jobKey) {
		String jobName = job.getJobName();
		String jobGroup = job.getJobGroup();

		CronScheduleBuilder cron = CronScheduleBuilder.cronSchedule(job.getCronExpression());
		cron = applyMisfire(cron, job.getMisfirePolicy());

		TriggerBuilder<CronTrigger> triggerBuilder = TriggerBuilder.newTrigger()
			.withIdentity(jobName + TRIGGER_SUFFIX, jobGroup)
			.forJob(jobKey)
			.withSchedule(cron);

		if (job.getStartTime() != null) {
			triggerBuilder.startAt(Date.from(job.getStartTime().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (job.getEndTime() != null) {
			triggerBuilder.endAt(Date.from(job.getEndTime().atZone(ZoneId.systemDefault()).toInstant()));
		}
		return triggerBuilder.build();
	}

}
