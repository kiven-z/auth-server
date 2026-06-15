/**
 * 通用微服务远程调用内置任务（{@link com.auth.service.system.schedule.task.builtin.BuiltinQuartzJob}）。
 * <p>
 * {@link com.auth.service.system.schedule.task.builtin.feign.FeignInvokeJob} 通过
 * {@code job_params} 指定 serviceName、path、method 与可选 headers/body，经负载均衡 HTTP 发起调用。
 * </p>
 *
 * @author Bunny
 */
package com.auth.service.system.schedule.task.builtin.feign;
