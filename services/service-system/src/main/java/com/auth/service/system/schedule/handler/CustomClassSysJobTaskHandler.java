package com.auth.service.system.schedule.handler;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.task.mode.custom.DelegatingCustomEntryJob;
import org.quartz.Job;
import org.springframework.stereotype.Component;

import static com.auth.service.system.common.exception.code.schedule.SystemScheduleResultCode.JOB_CLASS_EMPTY;
import static com.auth.service.system.common.exception.code.schedule.SystemScheduleResultCode.JOB_MODE_CUSTOM_FORBIDS_INVOKE_TARGET;

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
	public void validate(SysJobEntity job) {
		if (CharSequenceUtil.isBlank(job.getJobClass())) {
			throw new SystemBusinessException(JOB_CLASS_EMPTY);
		}
		if (CharSequenceUtil.isNotBlank(job.getInvokeTarget())) {
			throw new SystemBusinessException(JOB_MODE_CUSTOM_FORBIDS_INVOKE_TARGET);
		}
	}

	@Override
	public Class<? extends Job> resolveEntryJobClass(boolean concurrent) {
		return concurrent ? DelegatingCustomEntryJob.class : DelegatingCustomEntryJob.Disallow.class;
	}

}
