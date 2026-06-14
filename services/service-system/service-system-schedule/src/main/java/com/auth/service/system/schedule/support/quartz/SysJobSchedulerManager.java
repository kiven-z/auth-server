package com.auth.service.system.schedule.support.quartz;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.BooleanUtil;
import com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Quartz 调度写门面：注册、暂停、触发（不含运行时查询）
 *
 * @author Bunny
 */
@Slf4j
@Component
public class SysJobSchedulerManager {

	private final Scheduler scheduler;

	private final SysJobQuartzJobFactory sysJobQuartzJobFactory;

	private final SysJobQuartzTriggerFactory sysJobQuartzTriggerFactory;

	public SysJobSchedulerManager(@Lazy Scheduler scheduler, SysJobQuartzJobFactory sysJobQuartzJobFactory,
			SysJobQuartzTriggerFactory sysJobQuartzTriggerFactory) {
		this.scheduler = scheduler;
		this.sysJobQuartzJobFactory = sysJobQuartzJobFactory;
		this.sysJobQuartzTriggerFactory = sysJobQuartzTriggerFactory;
	}

	/**
	 * 删除 Quartz 中的 Job（若存在）
	 * @param jobKey 任务键
	 */
	public void deleteJobIfExists(JobKey jobKey) throws SchedulerException {
		if (scheduler.checkExists(jobKey)) {
			scheduler.deleteJob(jobKey);
		}
	}

	/**
	 * 按当前实体状态同步调度：启用时先删后建；停用时仅暂停 Quartz，不重建 Trigger
	 * @param job 任务实体
	 */
	public void scheduleOrReplace(SysJobEntity job) throws SchedulerException {
		String jobName = job.getJobName();
		String jobGroup = job.getJobGroup();
		JobKey jobKey = JobKey.jobKey(jobName, jobGroup);

		if (BooleanUtil.isFalse(job.getStatus())) {
			if (scheduler.checkExists(jobKey)) {
				pauseJob(jobName, jobGroup);
			}
			return;
		}

		deleteJobIfExists(jobKey);
		JobDetail detail = sysJobQuartzJobFactory.build(job);
		CronTrigger trigger = sysJobQuartzTriggerFactory.build(job, jobKey);
		scheduler.scheduleJob(detail, trigger);
	}

	/**
	 * 暂停调度
	 * @param jobName 任务名
	 * @param jobGroup 分组
	 */
	public void pauseJob(String jobName, String jobGroup) throws SchedulerException {
		JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
		if (scheduler.checkExists(jobKey)) {
			scheduler.pauseJob(jobKey);
		}
	}

	/**
	 * 暂停整个分组下的所有任务（仅暂停 Quartz 调度状态，不删除 Trigger）
	 * @param groupCode 分组编码
	 */
	public void pauseGroup(String groupCode) throws SchedulerException {
		if (CharSequenceUtil.isBlank(groupCode)) {
			return;
		}
		scheduler.pauseJobs(GroupMatcher.groupEquals(groupCode));
	}

	/**
	 * 立即触发一次（不影响 Cron）
	 * @param jobName 任务名
	 * @param jobGroup 分组
	 */
	public void triggerJob(String jobName, String jobGroup) throws SchedulerException {
		triggerJob(jobName, jobGroup, SysJobQuartzDataKeys.TRIGGER_TYPE_MANUAL);
	}

	/**
	 * 立即触发一次（不影响 Cron）
	 * @param jobName 任务名
	 * @param jobGroup 分组
	 * @param triggerType 触发类型
	 */
	public void triggerJob(String jobName, String jobGroup, String triggerType) throws SchedulerException {
		JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
		if (!scheduler.checkExists(jobKey)) {
			throw new SchedulerException("Job 不存在: " + jobKey);
		}

		JobDataMap onceMap = new JobDataMap();
		onceMap.put(SysJobQuartzDataKeys.TRIGGER_TYPE, triggerType);
		scheduler.triggerJob(jobKey, onceMap);
	}

	/**
	 * 确保 JobDetail 存在（不创建 Trigger），用于立即执行等场景
	 * @param job 任务实体
	 */
	public void ensureJobDetail(SysJobEntity job) throws SchedulerException {
		JobKey jobKey = JobKey.jobKey(job.getJobName(), job.getJobGroup());
		if (scheduler.checkExists(jobKey)) {
			return;
		}
		JobDetail detail = sysJobQuartzJobFactory.build(job);
		scheduler.addJob(detail, false);
	}

}
