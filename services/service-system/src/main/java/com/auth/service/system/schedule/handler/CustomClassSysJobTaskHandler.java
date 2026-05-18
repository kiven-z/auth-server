package com.auth.service.system.schedule.handler;

import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.task.mode.custom.DelegatingCustomEntryJob;
import org.quartz.Job;
import org.springframework.stereotype.Component;

/**
 * 自定义类模式处理器
 *
 * @author Bunny
 */
@Component
public class CustomClassSysJobTaskHandler implements SysJobTaskHandler {

	@Override
	public String handlerCode() {
		return "customClassHandler";
	}

	@Override
	public SysJobTaskType taskType() {
		return SysJobTaskType.CUSTOM_CLASS;
	}

	@Override
	public Class<? extends Job> resolveEntryJobClass(boolean concurrent) {
		return concurrent ? DelegatingCustomEntryJob.class : DelegatingCustomEntryJob.Disallow.class;
	}

}
