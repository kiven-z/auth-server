/**
 * 通用 Feign
 * 微服务调用内置任务（{@link com.auth.service.system.schedule.task.builtin.BuiltinQuartzJob}）。
 * <p>
 * 计划提供单一封装类，通过 {@code job_params} 指定服务名、路径与请求体，避免为每个微服务接口单独建 Job。
 * </p>
 *
 * @author Bunny
 */
package com.auth.service.system.schedule.task.builtin.feign;
