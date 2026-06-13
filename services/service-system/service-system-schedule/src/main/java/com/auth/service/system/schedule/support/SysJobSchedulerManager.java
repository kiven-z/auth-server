package com.auth.service.system.schedule.support;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.auth.service.system.schedule.handler.SysJobTaskHandler;
import com.auth.service.system.schedule.handler.SysJobTaskHandlerRegistry;
import com.auth.service.system.schedule.model.constans.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobQuartzRuntimeStatus;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

import static com.auth.service.system.schedule.model.constans.SysJobQuartzDataKeys.JOB_ZONE;
import static com.auth.service.system.schedule.model.constans.SysJobQuartzDataKeys.TRIGGER_SUFFIX;

/**
 * 将 {@link SysJobEntity} 与 Quartz {@link Scheduler} 同步（增删改、暂停、恢复、立即执行）
 *
 * @author Bunny
 */
@Slf4j
@Component
public class SysJobSchedulerManager {

	private final Scheduler scheduler;

	private final SysJobTaskHandlerRegistry taskHandlerRegistry;

	public SysJobSchedulerManager(@Lazy Scheduler scheduler, SysJobTaskHandlerRegistry taskHandlerRegistry) {
		this.scheduler = scheduler;
		this.taskHandlerRegistry = taskHandlerRegistry;
	}

	// =============== 静态工具方法 ===============

	/**
	 * Trigger 名称规则：{@code jobName + "_TRIGGER"}
	 * @param jobName 任务名
	 * @return Trigger 名
	 */
	public static String triggerName(String jobName) {
		return jobName + TRIGGER_SUFFIX;
	}

	/**
	 * 合并 JSON 参数到 JobDataMap
	 * @param map JobDataMap
	 * @param jobParamsJson 参数 JSON 字符串
	 */
	private static void mergeJsonParams(JobDataMap map, String jobParamsJson) {
		// 如果 jobParamsJson 为空，则返回
		if (CharSequenceUtil.isBlank(jobParamsJson)) {
			return;
		}

		try {
			// JSONObject 实现了 Map 接口
			JSONObject json = JSONUtil.parseObj(jobParamsJson);
			map.putAll(json);
		}
		catch (Exception e) {
			log.warn("解析 job_params 失败: {}", jobParamsJson, e);
			map.put("jobParamsRaw", jobParamsJson);
		}
	}

	/**
	 * 应用 Misfire 策略 - 1 表示补跑所有 - 2 表示补跑一次 - 3 表示不补跑
	 * @param cron CronScheduleBuilder
	 * @param policy Misfire 策略
	 * @return CronScheduleBuilder
	 */
	private static CronScheduleBuilder applyMisfire(CronScheduleBuilder cron, Integer policy) {
		// 如果 policy 为空，则默认值为 2 （补跑一次）
		int p = Objects.requireNonNullElse(policy, 2);

		// 根据 policy 返回不同的 CronScheduleBuilder
		return switch (p) {
			// 补跑所有
			case 1 -> cron.withMisfireHandlingInstructionIgnoreMisfires();
			// 补跑一次
			case 2 -> cron.withMisfireHandlingInstructionFireAndProceed();
			// 不补跑
			case 3 -> cron.withMisfireHandlingInstructionDoNothing();
			// 默认值为 2 （补跑一次）
			default -> cron.withMisfireHandlingInstructionFireAndProceed();
		};
	}

	// =============== 调度管理方法 ===============

	/**
	 * 删除 Quartz 中的 Job （若存在）
	 * @param jobKey 任务键
	 */
	public void deleteJobIfExists(JobKey jobKey) throws SchedulerException {
		boolean exists = scheduler.checkExists(jobKey);
		if (exists) {
			scheduler.deleteJob(jobKey);
		}
	}

	/**
	 * 按当前实体状态重建调度：先删后建；若 {@code status != 1} 则仅删除不落调度
	 * @param job 任务实体
	 */
	public void scheduleOrReplace(SysJobEntity job) throws SchedulerException {
		String jobName = job.getJobName();
		String jobGroup = job.getJobGroup();

		// 当前任务存在并且为暂停直接返回，不做任何操作
		JobKey jobKey = JobKey.jobKey(jobName, jobGroup);

		if (BooleanUtil.isFalse(job.getStatus())) {
			if (scheduler.checkExists(jobKey)) {
				pauseJob(jobName, jobGroup);
			}
			return;
		}

		// 先删除 Job
		deleteJobIfExists(jobKey);
		// 构建 JobDetail
		JobDetail detail = buildJobDetail(job);

		// 构建 CronTrigger
		CronTrigger trigger = buildCronTrigger(job, jobKey);

		// 调度 Job
		scheduler.scheduleJob(detail, trigger);
	}

	/**
	 * 暂停调度
	 * @param jobName 任务名
	 * @param jobGroup 分组
	 */
	public void pauseJob(String jobName, String jobGroup) throws SchedulerException {
		// 构建 JobKey
		JobKey jk = JobKey.jobKey(jobName, jobGroup);

		// 如果 Job 存在，则暂停调度
		if (scheduler.checkExists(jk)) {
			scheduler.pauseJob(jk);
		}
	}

	/**
	 * 恢复调度
	 * @param jobName 任务名
	 * @param jobGroup 分组
	 */
	public void resumeJob(String jobName, String jobGroup) throws SchedulerException {
		// 构建 JobKey
		JobKey jk = JobKey.jobKey(jobName, jobGroup);

		// 如果 Job 存在，则恢复调度
		if (scheduler.checkExists(jk)) {
			scheduler.resumeJob(jk);
		}
	}

	/**
	 * 恢复整个分组下的所有任务（仅恢复 Quartz 调度状态，不重建 Trigger）
	 * @param groupCode 分组编码
	 */
	public void resumeGroup(String groupCode) throws SchedulerException {
		// 如果分组编码为空，则返回
		if (groupCode == null || groupCode.isBlank()) {
			return;
		}

		// 恢复分组下的所有任务
		scheduler.resumeJobs(GroupMatcher.groupEquals(groupCode));
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
		// 构建 JobKey
		JobKey jk = JobKey.jobKey(jobName, jobGroup);

		// 如果 Job 不存在，则抛出异常
		if (!scheduler.checkExists(jk)) {
			throw new SchedulerException("Job 不存在: " + jk);
		}

		JobDataMap onceMap = new JobDataMap();
		onceMap.put(SysJobQuartzDataKeys.TRIGGER_TYPE, triggerType);
		scheduler.triggerJob(jk, onceMap);
	}

	/**
	 * 确保 JobDetail 存在（不创建 Trigger），用于立即执行等场景
	 * @param job 任务实体
	 */
	public void ensureJobDetail(SysJobEntity job) throws SchedulerException {
		// 构建 JobKey
		JobKey jk = JobKey.jobKey(job.getJobName(), job.getJobGroup());

		// 如果 Job 存在，则返回
		if (scheduler.checkExists(jk)) {
			return;
		}

		// 构建 JobDetail
		JobDetail detail = buildJobDetail(job);

		// 添加 Job
		scheduler.addJob(detail, false);
	}

	/**
	 * 构建 JobDetail
	 * @param job 任务实体
	 * @return JobDetail
	 */
	private JobDetail buildJobDetail(SysJobEntity job) {
		Long id = job.getId();
		String invokeTarget = job.getInvokeTarget();
		String jobParams = job.getJobParams();
		String jobName = job.getJobName();
		String jobGroup = job.getJobGroup();
		Boolean jobConcurrent = job.getConcurrent();
		SysJobTaskHandler handler = taskHandlerRegistry.resolve(job);
		SysJobTaskType resolvedTaskType = SysJobTaskType.resolve(job.getTaskType());

		// 判断是否并发
		boolean concurrent = BooleanUtil.isTrue(jobConcurrent);
		Class<? extends Job> jobClass = handler.resolveEntryJobClass(concurrent);

		// 构建 JobDataMap
		JobDataMap map = new JobDataMap();
		// 添加任务 ID
		map.put(SysJobQuartzDataKeys.SYS_JOB_ID, String.valueOf(id));
		// 添加调用目标
		map.put(SysJobQuartzDataKeys.INVOKE_TARGET, invokeTarget);
		// 添加任务类
		map.put(SysJobQuartzDataKeys.JOB_CLASS, job.getJobClass());
		map.put(SysJobQuartzDataKeys.TASK_TYPE, resolvedTaskType.name());
		map.put(SysJobQuartzDataKeys.HANDLER_CODE, handler.handlerCode());
		map.put(SysJobQuartzDataKeys.PAYLOAD_JSON, job.getPayloadJson());
		map.put(SysJobQuartzDataKeys.TRIGGER_TYPE, SysJobQuartzDataKeys.TRIGGER_TYPE_SCHEDULE);

		// 合并 JSON 参数
		mergeJsonParams(map, jobParams);
		// 构建 JobDetail

		return JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroup).usingJobData(map).storeDurably().build();
	}

	/**
	 * 构建 CronTrigger
	 * @param job 任务实体
	 * @param jobKey 任务键
	 * @return CronTrigger
	 */
	private CronTrigger buildCronTrigger(SysJobEntity job, JobKey jobKey) {
		String cronExpression = job.getCronExpression();
		Integer misfirePolicy = job.getMisfirePolicy();
		String jobName = job.getJobName();
		String jobGroup = job.getJobGroup();

		// 构建 CronScheduleBuilder
		CronScheduleBuilder cron = CronScheduleBuilder.cronSchedule(cronExpression)
			.inTimeZone(TimeZone.getTimeZone(JOB_ZONE));
		// 应用 Misfire 策略
		cron = applyMisfire(cron, misfirePolicy);

		// 构建 TriggerBuilder

		TriggerBuilder<CronTrigger> tb = TriggerBuilder.newTrigger()
			.withIdentity(triggerName(jobName), jobGroup)
			// 添加任务键
			.forJob(jobKey)
			// 添加 CronScheduleBuilder
			.withSchedule(cron);

		// 添加开始时间
		if (job.getStartTime() != null) {
			tb.startAt(Date.from(job.getStartTime().atZone(JOB_ZONE).toInstant()));
		}

		// 添加结束时间
		if (job.getEndTime() != null) {
			tb.endAt(Date.from(job.getEndTime().atZone(JOB_ZONE).toInstant()));
		}

		// 构建 CronTrigger
		return tb.build();
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
	 * 批量解析 Quartz 运行时状态（复用一次 {@code getCurrentlyExecutingJobs}）
	 * @param jobKeys 任务键集合
	 * @return 任务键 → 快照
	 */
	public Map<JobKey, SysJobQuartzRuntimeSnapshot> batchResolveRuntimeSnapshots(Collection<JobKey> jobKeys) {
		Map<JobKey, SysJobQuartzRuntimeSnapshot> result = new HashMap<>();
		if (jobKeys == null || jobKeys.isEmpty()) {
			return result;
		}
		try {
			Set<JobKey> runningJobKeys = collectRunningJobKeys();
			for (JobKey jobKey : jobKeys) {
				result.put(jobKey, resolveRuntimeSnapshot(jobKey, runningJobKeys));
			}
		}
		catch (SchedulerException exception) {
			log.warn("批量解析 Quartz 运行时状态失败", exception);
			for (JobKey jobKey : jobKeys) {
				result.putIfAbsent(jobKey, SysJobQuartzRuntimeSnapshot.of(SysJobQuartzRuntimeStatus.ERROR, null));
			}
		}
		return result;
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
		if (triggerStatus != null) {
			return SysJobQuartzRuntimeSnapshot.of(triggerStatus, null);
		}
		return SysJobQuartzRuntimeSnapshot.of(SysJobQuartzRuntimeStatus.IDLE, null);
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
		if (triggers == null || triggers.isEmpty()) {
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
			java.util.List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
			if (triggers == null || triggers.isEmpty()) {
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

	/**
	 * 触发时间值对象
	 */
	public record FireTimes(Date previousFireTime, Date nextFireTime) {

		public static FireTimes empty() {
			return new FireTimes(null, null);
		}
	}

}
