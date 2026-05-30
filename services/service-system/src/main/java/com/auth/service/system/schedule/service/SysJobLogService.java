package com.auth.service.system.schedule.service;

import com.auth.service.system.log.model.entity.JobLogEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import org.quartz.JobExecutionException;

/**
 * 定时任务执行日志
 *
 * @author Bunny
 */
public interface SysJobLogService extends IService<JobLogEntity> {

	/**
	 * 记录成功日志（由 Quartz 监听调用）
	 * @param jobId 任务ID
	 * @param jobName 任务名称
	 * @param jobGroup 任务组
	 * @param invokeTarget 任务执行目标
	 * @param elapsedMs 执行时间
	 * @param message 消息
	 */
	void recordSuccess(Long jobId, String jobName, String jobGroup, String invokeTarget, String triggerType,
			long elapsedMs, String message);

	/**
	 * 记录失败日志（由 Quartz 监听调用）
	 * @param jobId 任务ID
	 * @param jobName 任务名称
	 * @param jobGroup 任务组
	 * @param invokeTarget 任务执行目标
	 * @param elapsedMs 执行时间
	 * @param ex 异常
	 */
	void recordFailure(Long jobId, String jobName, String jobGroup, String invokeTarget, String triggerType,
			long elapsedMs, JobExecutionException ex);

}
