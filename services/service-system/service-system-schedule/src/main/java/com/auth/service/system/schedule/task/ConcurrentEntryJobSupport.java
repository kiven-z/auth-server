package com.auth.service.system.schedule.task;

import lombok.experimental.UtilityClass;
import org.quartz.Job;

/**
 * Quartz 入口 Job 并发策略工厂（全局仅一对 EntryJob 类）
 *
 * @author Bunny
 */
@UtilityClass
public final class ConcurrentEntryJobSupport {

	/**
	 * 按并发策略返回统一入口 Job 类型
	 * @param concurrent 是否允许并发
	 * @return 入口 Job 类
	 */
	public static Class<? extends Job> resolveEntryJobClass(boolean concurrent) {
		return concurrent ? SysJobEntryJob.class : SysJobEntryJob.Disallow.class;
	}

}
