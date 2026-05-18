package com.auth.service.system.schedule.handler;

import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import org.quartz.Job;

/**
 * 定时任务执行处理器（调度层只关心类型，不关心业务实现）
 *
 * @author Bunny
 */
public interface SysJobTaskHandler {

	/**
	 * 处理器编码（供 task handlerCode 绑定）
	 * @return 编码
	 */
	String handlerCode();

	/**
	 * 支持的任务类型
	 * @return 任务类型
	 */
	SysJobTaskType taskType();

	/**
	 * 校验任务定义合法性
	 * @param job 任务定义
	 */
	void validate(SysJobEntity job);

	/**
	 * 根据并发策略返回 Quartz 入口 Job 类型
	 * @param concurrent 是否并发
	 * @return 入口 Job
	 */
	Class<? extends Job> resolveEntryJobClass(boolean concurrent);

}
