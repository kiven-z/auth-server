/**
 * 通用 HTTP 内置任务（{@link com.auth.service.system.schedule.task.builtin.BuiltinQuartzJob}）。
 * <p>
 * 计划按 HTTP 动词拆分独立 Job 类（如 GET、POST、DELETE），参数通过 {@code job_params} JSON 配置。
 * </p>
 *
 * @author Bunny
 */
package com.auth.service.system.schedule.task.builtin.http;
