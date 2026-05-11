package com.auth.service.system.schedule.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.auth.service.system.schedule.mapper.JobLogMapper;
import com.auth.service.system.schedule.model.entity.JobLogEntity;
import com.auth.service.system.schedule.service.SysJobLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 任务执行日志实现
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysJobLogServiceImpl extends ServiceImpl<JobLogMapper, JobLogEntity> implements SysJobLogService {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void recordSuccess(Long jobId, String jobName, String jobGroup, String invokeTarget, long elapsedMs,
			String message) {
		JobLogEntity sysJobLog = new JobLogEntity();
		sysJobLog.setJobName(jobName);
		sysJobLog.setJobGroup(jobGroup);
		sysJobLog.setInvokeTarget(invokeTarget);
		sysJobLog.setJobMessage(message);
		sysJobLog.setStatus(false);
		sysJobLog.setElapsedTime(elapsedMs);
		sysJobLog.setCreateTime(LocalDateTime.now());
		save(sysJobLog);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void recordFailure(Long jobId, String jobName, String jobGroup, String invokeTarget, long elapsedMs,
			JobExecutionException ex) {
		JobLogEntity jobLog = new JobLogEntity();
		jobLog.setJobName(jobName);
		jobLog.setJobGroup(jobGroup);
		jobLog.setInvokeTarget(invokeTarget);
		jobLog.setJobMessage("FAILED");
		jobLog.setStatus(true);
		jobLog.setElapsedTime(elapsedMs);
		jobLog.setCreateTime(LocalDateTime.now());

		// 如果异常不为空，则设置异常信息
		if (ex != null) {
			jobLog.setExceptionInfo(ExceptionUtil.stacktraceToString(ex, 8000));
		}

		save(jobLog);
	}

}
