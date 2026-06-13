package com.auth.service.system.schedule.support;

import com.auth.service.system.schedule.model.enums.SysJobQuartzRuntimeStatus;

import java.time.LocalDateTime;

/**
 * Quartz 运行时状态快照（不含失败日志补充）
 *
 * @author Bunny
 */
public record SysJobQuartzRuntimeSnapshot(SysJobQuartzRuntimeStatus status, LocalDateTime fireTime) {

	/**
	 * 构建快照
	 * @param status 运行时状态
	 * @param fireTime 开始执行时间
	 * @return 快照
	 */
	public static SysJobQuartzRuntimeSnapshot of(SysJobQuartzRuntimeStatus status, LocalDateTime fireTime) {
		return new SysJobQuartzRuntimeSnapshot(status, fireTime);
	}

}
