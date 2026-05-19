package com.auth.service.system.schedule.model.enums;

import com.auth.service.system.common.exception.SystemBusinessException;

import static com.auth.service.system.schedule.exception.SystemScheduleResultCode.JOB_TASK_TYPE_UNSUPPORTED;

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

}
