package com.auth.service.system.schedule.validation.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.validation.SysJobTaskTypeDefinitionValidator;
import org.springframework.stereotype.Component;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_CLASS_EMPTY;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_MODE_CUSTOM_FORBIDS_INVOKE_TARGET;

/**
 * 自定义类任务定义校验器
 *
 * @author Bunny
 */
@Component
public class CustomClassTaskDefinitionValidator implements SysJobTaskTypeDefinitionValidator {

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

}
