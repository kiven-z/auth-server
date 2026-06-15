package com.auth.service.system.schedule.task.builtin.validation;

/**
 * 内置 {@link org.quartz.Job} 的 {@code job_params} 定义层校验 SPI（保存/更新任务时触发）
 *
 * @author Bunny
 */
public interface BuiltinJobDefinitionValidator {

	/**
	 * 支持的内置 Job 全限定类名
	 * @return 类全限定名
	 */
	String jobClass();

	/**
	 * 校验 {@code job_params} JSON
	 * @param jobParamsJson 原始 JSON
	 */
	void validateJobParams(String jobParamsJson);

}
