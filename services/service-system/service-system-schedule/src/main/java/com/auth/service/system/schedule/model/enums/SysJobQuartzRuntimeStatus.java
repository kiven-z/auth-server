package com.auth.service.system.schedule.model.enums;

/**
 * Quartz 任务运行时状态（与 {@code sys_job.status} 调度启停无关）
 *
 * @author Bunny
 */
public enum SysJobQuartzRuntimeStatus {

	/**
	 * 未注册到 Quartz
	 */
	NOT_REGISTERED,

	/**
	 * 已注册且空闲
	 */
	IDLE,

	/**
	 * 正在执行
	 */
	RUNNING,

	/**
	 * 已触发但等待执行（如禁止并发时的 BLOCKED）
	 */
	PENDING,

	/**
	 * Quartz 侧已暂停
	 */
	PAUSED,

	/**
	 * Trigger 处于 ERROR
	 */
	ERROR,

	/**
	 * 最近一次执行失败且当前未在运行
	 */
	FAILED

}
