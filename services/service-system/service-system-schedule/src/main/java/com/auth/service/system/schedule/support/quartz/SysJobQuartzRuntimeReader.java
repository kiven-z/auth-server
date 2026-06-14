package com.auth.service.system.schedule.support.quartz;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.schedule.model.enums.SysJobQuartzRuntimeStatus;
import com.auth.service.system.schedule.support.SysJobQuartzRuntimeSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys.JOB_ZONE;

/**
 * 读取 Quartz 运行时快照与触发时间（只读，不参与调度写入）
 *
 * @author Bunny
 */
@Slf4j
@Component
public class SysJobQuartzRuntimeReader {

	private final Scheduler scheduler;

	public SysJobQuartzRuntimeReader(@Lazy Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	/**
	 * 解析单个任务的 Quartz 运行时状态
	 * @param jobName 任务名
	 * @param jobGroup 分组
	 * @return 运行时快照
	 */
	public SysJobQuartzRuntimeSnapshot resolveRuntimeSnapshot(String jobName, String jobGroup) {
		try {
			return resolveRuntimeSnapshot(JobKey.jobKey(jobName, jobGroup), collectRunningJobKeys());
		}
		catch (SchedulerException exception) {
			log.warn("解析 Quartz 运行时状态失败: {} / {}", jobGroup, jobName, exception);
			return SysJobQuartzRuntimeSnapshot.of(SysJobQuartzRuntimeStatus.ERROR, null);
		}
	}

	/**
	 * 查询任务触发时间
	 * @param jobName 任务名
	 * @param jobGroup 分组
	 * @return 触发时间
	 */
	public FireTimes getFireTimes(String jobName, String jobGroup) {
		try {
			JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
			if (!scheduler.checkExists(jobKey)) {
				return FireTimes.empty();
			}
			List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
			if (CollUtil.isEmpty(triggers)) {
				return FireTimes.empty();
			}
			Trigger trigger = triggers.get(0);
			return new FireTimes(trigger.getPreviousFireTime(), trigger.getNextFireTime());
		}
		catch (Exception exception) {
			log.warn("查询触发时间失败: {} / {}", jobGroup, jobName, exception);
			return FireTimes.empty();
		}
	}

	private Set<JobKey> collectRunningJobKeys() throws SchedulerException {
		Set<JobKey> runningJobKeys = new HashSet<>();
		for (JobExecutionContext context : scheduler.getCurrentlyExecutingJobs()) {
			runningJobKeys.add(context.getJobDetail().getKey());
		}
		return runningJobKeys;
	}

	private SysJobQuartzRuntimeSnapshot resolveRuntimeSnapshot(JobKey jobKey, Set<JobKey> runningJobKeys)
			throws SchedulerException {
		if (!scheduler.checkExists(jobKey)) {
			return SysJobQuartzRuntimeSnapshot.of(SysJobQuartzRuntimeStatus.NOT_REGISTERED, null);
		}
		if (runningJobKeys.contains(jobKey)) {
			LocalDateTime fireTime = resolveRunningFireTime(jobKey);
			return SysJobQuartzRuntimeSnapshot.of(SysJobQuartzRuntimeStatus.RUNNING, fireTime);
		}
		SysJobQuartzRuntimeStatus triggerStatus = resolveTriggerStatus(jobKey);
		return SysJobQuartzRuntimeSnapshot.of(Objects.requireNonNullElse(triggerStatus, SysJobQuartzRuntimeStatus.IDLE),
				null);
	}

	private LocalDateTime resolveRunningFireTime(JobKey jobKey) throws SchedulerException {
		for (JobExecutionContext context : scheduler.getCurrentlyExecutingJobs()) {
			if (jobKey.equals(context.getJobDetail().getKey())) {
				return toLocalDateTime(context.getFireTime());
			}
		}
		return null;
	}

	private SysJobQuartzRuntimeStatus resolveTriggerStatus(JobKey jobKey) throws SchedulerException {
		List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
		if (CollUtil.isEmpty(triggers)) {
			return null;
		}
		boolean paused = false;
		for (Trigger trigger : triggers) {
			Trigger.TriggerState state = scheduler.getTriggerState(trigger.getKey());
			if (state == Trigger.TriggerState.BLOCKED) {
				return SysJobQuartzRuntimeStatus.PENDING;
			}
			if (state == Trigger.TriggerState.ERROR) {
				return SysJobQuartzRuntimeStatus.ERROR;
			}
			if (state == Trigger.TriggerState.PAUSED) {
				paused = true;
			}
		}
		if (paused) {
			return SysJobQuartzRuntimeStatus.PAUSED;
		}
		return null;
	}

	private LocalDateTime toLocalDateTime(Date date) {
		if (date == null) {
			return null;
		}
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), JOB_ZONE);
	}

	/**
	 * 触发时间值对象
	 */
	public record FireTimes(Date previousFireTime, Date nextFireTime) {

		public static FireTimes empty() {
			return new FireTimes(null, null);
		}
	}

}
