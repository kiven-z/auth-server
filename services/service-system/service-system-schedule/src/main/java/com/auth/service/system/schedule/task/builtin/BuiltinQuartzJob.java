package com.auth.service.system.schedule.task.builtin;

import org.quartz.Job;

/**
 * 内置 {@link Job} 标记：通过 {@code CUSTOM_CLASS} 调度，从 {@code job_params} 读取 JSON 参数。
 * <p>
 * 生产级内置实现见 {@link HttpInvokeJob}、{@link FeignInvokeJob}；演示代码保留在 {@code example} 包。
 * </p>
 * <p>
 * 阅读路径：{@code SysJobEntryJob} → {@code CustomClassTaskExecutor} → 具体内置 Job； 保存时校验
 * {@code CustomClassTaskDefinitionValidator} →
 * {@link com.auth.service.system.schedule.validation.builtin.BuiltinJobDefinitionValidatorRegistry}；
 * 内置校验映射见
 * {@link com.auth.service.system.schedule.validation.builtin.BuiltinJobDefinitionValidatorConfig}。
 * </p>
 *
 * @author Bunny
 */
public interface BuiltinQuartzJob extends Job {

}
