package com.auth.service.system.schedule.support;

/**
 * 任务熔断流程异常（如熔断后暂停 Quartz 失败，需回滚数据库事务）
 *
 * @author Bunny
 */
public class SysJobCircuitBreakerException extends RuntimeException {

	/**
	 * @param message 错误说明
	 * @param cause Quartz 调度异常
	 */
	public SysJobCircuitBreakerException(String message, Throwable cause) {
		super(message, cause);
	}

}
