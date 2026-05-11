package com.auth.service.system.schedule.support;

import cn.hutool.core.util.BooleanUtil;
import com.auth.service.system.log.model.entity.JobLogEntity;
import com.auth.service.system.schedule.config.ScheduleJobProperties;
import com.auth.service.system.schedule.mapper.JobLogMapper;
import com.auth.service.system.schedule.mapper.SysJobMapper;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 根据最近执行日志连续失败次数触发熔断：更新 {@code sys_job.status=0} 并 {@code pauseJob}
 *
 * @author Bunny
 */
@Slf4j
@Component
public class SysJobCircuitBreaker {

	/**
	 * 调度器
	 */
	private final Scheduler scheduler;

	/**
	 * 系统任务Mapper
	 */
	private final SysJobMapper sysJobMapper;

	/**
	 * 系统任务日志Mapper
	 */
	private final JobLogMapper jobLogMapper;

	/**
	 * 定时任务配置
	 */
	private final ScheduleJobProperties scheduleJobProperties;

	public SysJobCircuitBreaker(@Lazy Scheduler scheduler, SysJobMapper sysJobMapper, JobLogMapper jobLogMapper,
			ScheduleJobProperties scheduleJobProperties) {
		this.scheduler = scheduler;
		this.sysJobMapper = sysJobMapper;
		this.jobLogMapper = jobLogMapper;
		this.scheduleJobProperties = scheduleJobProperties;
	}

	/**
	 * 在写入一条失败日志后评估是否熔断
	 * @param jobId 业务任务主键（可为空则跳过）
	 * @param jobName 任务名
	 * @param jobGroup 分组
	 */
	@Transactional(rollbackFor = Exception.class)
	public void evaluateAfterFailure(Long jobId, String jobName, String jobGroup) {
		int threshold = scheduleJobProperties.getConsecutiveFailureThreshold();
		if (threshold <= 0 || jobId == null) {
			return;
		}

		// 1. 检查任务是否已暂停
		SysJobEntity job = sysJobMapper.selectById(jobId);
		if (job == null || BooleanUtil.isFalse(job.getStatus())) {
			return;
		}

		// 2. 查询最近连续失败日志
		List<JobLogEntity> recent = jobLogMapper.selectRecentByJobNameAndGroup(jobName, jobGroup, threshold);
		if (recent.size() < threshold) {
			return;
		}
		// 检查是否【全部】为【false】
		boolean allFailed = recent.stream().allMatch(log -> BooleanUtil.isFalse(log.getStatus()));
		if (!allFailed) {
			return;
		}

		// 3. 更新数据库状态
		LambdaUpdateWrapper<SysJobEntity> wrapper = new LambdaUpdateWrapper<>();
		wrapper.eq(SysJobEntity::getId, jobId).set(SysJobEntity::getStatus, false);
		sysJobMapper.update(null, wrapper);

		// 4. 暂停 Quartz 任务（失败则回滚事务）
		try {
			scheduler.pauseJob(JobKey.jobKey(jobName, jobGroup));
		}
		catch (Exception e) {
			log.error("熔断后暂停 Quartz 任务失败: {}.{}", jobGroup, jobName, e);
			throw new RuntimeException("熔断暂停任务失败", e);
		}

		log.warn("任务已连续失败 {} 次，已暂停: {} / {}", threshold, jobGroup, jobName);
	}

}
