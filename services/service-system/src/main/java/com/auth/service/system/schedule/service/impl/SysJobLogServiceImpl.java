package com.auth.service.system.schedule.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.auth.service.system.log.model.entity.JobLogEntity;
import com.auth.service.system.schedule.mapper.JobLogMapper;
import com.auth.service.system.schedule.service.SysJobLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 任务执行日志实现（追加写入 {@code log_job}，删除由管理端物理删除）。
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysJobLogServiceImpl extends ServiceImpl<JobLogMapper, JobLogEntity> implements SysJobLogService {

	/**
	 * 组装公共字段；审计列由 MyBatis 填充（无登录用户时为 NULL）。
	 */
	private static JobLogEntity buildBaseLog(Long jobId, String jobName, String jobGroup, String invokeTarget,
			String triggerType, long elapsedMs) {
		JobLogEntity entity = new JobLogEntity();
		entity.setJobId(jobId);
		entity.setJobName(jobName);
		entity.setJobGroup(jobGroup);
		entity.setInvokeTarget(invokeTarget);
		entity.setTriggerType(triggerType);
		entity.setElapsedTime(elapsedMs);
		return entity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void recordSuccess(Long jobId, String jobName, String jobGroup, String invokeTarget, String triggerType,
			long elapsedMs, String message) {
		JobLogEntity entity = buildBaseLog(jobId, jobName, jobGroup, invokeTarget, triggerType, elapsedMs);
		entity.setJobMessage(message);
		entity.setStatus(true);
		save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void recordFailure(Long jobId, String jobName, String jobGroup, String invokeTarget, String triggerType,
			long elapsedMs, JobExecutionException ex) {
		JobLogEntity entity = buildBaseLog(jobId, jobName, jobGroup, invokeTarget, triggerType, elapsedMs);
		entity.setJobMessage("FAILED");
		entity.setStatus(false);
		if (ex != null) {
			entity.setExceptionInfo(ExceptionUtil.stacktraceToString(ex, 8000));
		}
		save(entity);
	}

}
