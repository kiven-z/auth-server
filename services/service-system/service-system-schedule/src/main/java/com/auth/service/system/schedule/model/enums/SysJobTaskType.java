package com.auth.service.system.schedule.model.enums;

import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.schedule.task.mode.bean.BeanInvokeEntryJob;
import com.auth.service.system.schedule.task.mode.custom.DelegatingCustomEntryJob;
import com.auth.service.system.schedule.task.mode.message.MessageDispatchEntryJob;
import org.quartz.Job;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_TASK_TYPE_UNSUPPORTED;

/**
 * 定时任务类型（仅表达调度执行入口，不承载业务语义）
 *
 * @author Bunny
 */
public enum SysJobTaskType {

	/**
	 * Bean 调用模式：执行 invoke_target
	 */
	BEAN_INVOKE,

	/**
	 * 自定义类模式：执行 job_class（需实现 Quartz Job）
	 */
	CUSTOM_CLASS,

	/**
	 * 统一消息派发模式：执行 payload_json 中的 TemplateMessageCommand
	 */
	MESSAGE_DISPATCH;

	/**
	 * 解析任务类型
	 * @param taskTypeRaw taskType 原始值
	 * @return 任务类型
	 */
	public static SysJobTaskType resolve(String taskTypeRaw) {
		try {
			return SysJobTaskType.valueOf(taskTypeRaw);
		}
		catch (Exception exception) {
			throw new SystemBusinessException(JOB_TASK_TYPE_UNSUPPORTED, taskTypeRaw);
		}
	}

	/**
	 * 默认处理器编码（与 {@code sys_job.handler_code} 一致）
	 * @return 处理器编码
	 */
	public String defaultHandlerCode() {
		return switch (this) {
			case BEAN_INVOKE -> "beanInvokeHandler";
			case CUSTOM_CLASS -> "customClassHandler";
			case MESSAGE_DISPATCH -> "messageDispatchHandler";
		};
	}

	/**
	 * 根据并发策略返回 Quartz 入口 Job 类型
	 * @param concurrent 是否允许并发
	 * @return 入口 Job 类
	 */
	public Class<? extends Job> resolveEntryJobClass(boolean concurrent) {
		if (concurrent) {
			return concurrentEntryJobClass();
		}
		return nonConcurrentEntryJobClass();
	}

	private Class<? extends Job> concurrentEntryJobClass() {
		return switch (this) {
			case BEAN_INVOKE -> BeanInvokeEntryJob.class;
			case CUSTOM_CLASS -> DelegatingCustomEntryJob.class;
			case MESSAGE_DISPATCH -> MessageDispatchEntryJob.class;
		};
	}

	private Class<? extends Job> nonConcurrentEntryJobClass() {
		return switch (this) {
			case BEAN_INVOKE -> BeanInvokeEntryJob.Disallow.class;
			case CUSTOM_CLASS -> DelegatingCustomEntryJob.Disallow.class;
			case MESSAGE_DISPATCH -> MessageDispatchEntryJob.Disallow.class;
		};
	}

}
