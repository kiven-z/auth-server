package com.auth.service.system.schedule.model.enums;

import cn.hutool.core.text.CharSequenceUtil;

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
	CUSTOM_CLASS;

	/**
	 * 兼容历史数据：未配置 taskType 时按 invokeTarget 是否为空推断
	 * @param taskTypeRaw taskType 原始值
	 * @param invokeTarget 调用目标
	 * @return 任务类型
	 */
	public static SysJobTaskType resolve(String taskTypeRaw, String invokeTarget) {
		if (CharSequenceUtil.isNotBlank(taskTypeRaw)) {
			return SysJobTaskType.valueOf(taskTypeRaw);
		}
		return CharSequenceUtil.isBlank(invokeTarget) ? CUSTOM_CLASS : BEAN_INVOKE;
	}

}
