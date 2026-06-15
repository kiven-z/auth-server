package com.auth.service.system.schedule.task.builtin;

import org.quartz.Job;

/**
 * 内置 {@link org.quartz.Job} 标记：通过 {@code CUSTOM_CLASS} 调度，从 {@code job_params} 读取 JSON
 * 参数。
 * <p>
 * 生产级内置实现按能力分子包：
 * </p>
 * <ul>
 * <li>{@code task.builtin.http} — 通用 HTTP 调用（GET/POST/DELETE 等）</li>
 * <li>{@code task.builtin.feign} — 通用 Feign 微服务调用</li>
 * </ul>
 * <p>
 * 演示代码保留在 {@code example} 包，与生产内置类分离。
 * </p>
 *
 * @author Bunny
 */
public interface BuiltinQuartzJob extends Job {

}
