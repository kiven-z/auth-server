package com.auth.service.system.schedule.validation.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.support.QuartzTaskRegistry;
import com.auth.service.system.schedule.validation.SysJobTaskTypeDefinitionValidator;
import com.auth.service.system.schedule.validation.SysJobTaskTypeFieldRules;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_CLASS_LOAD_FAILED;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_CLASS_NOT_WHITELISTED;

/**
 * 自定义类任务定义校验器
 *
 * @author Bunny
 */
@Component
@RequiredArgsConstructor
public class CustomClassTaskDefinitionValidator implements SysJobTaskTypeDefinitionValidator {

	private final QuartzTaskRegistry quartzTaskRegistry;

	@Override
	public SysJobTaskType taskType() {
		return SysJobTaskType.CUSTOM_CLASS;
	}

	@Override
	public void validate(SysJobEntity job) {
		SysJobTaskTypeFieldRules
			.detect(SysJobTaskType.CUSTOM_CLASS, job.getJobClass(), job.getInvokeTarget(), job.getPayloadJson(), true)
			.ifPresent(SysJobTaskTypeFieldRules::throwIfPresent);

		String jobClass = job.getJobClass();
		if (!quartzTaskRegistry.isAllowed(jobClass)) {
			throw new SysJobException(JOB_CLASS_NOT_WHITELISTED);
		}
		try {
			Class.forName(CharSequenceUtil.nullToEmpty(jobClass));
		}
		catch (ClassNotFoundException exception) {
			throw new SysJobException(JOB_CLASS_LOAD_FAILED, exception.getMessage());
		}
	}

}
