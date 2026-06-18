package com.auth.service.system.schedule.validation.task;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.support.catalog.QuartzTaskRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_CLASS_INVALID;

/**
 * 自定义类任务定义校验器
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class CustomClassTaskDefinitionValidator implements SysJobTaskTypeDefinitionValidator {

	private final QuartzTaskRegistry quartzTaskRegistry;

	private final BuiltinJobParamsValidator builtinJobParamsValidator;

	@Override
	public SysJobTaskType taskType() {
		return SysJobTaskType.CUSTOM_CLASS;
	}

	@Override
	public void validate(SysJobEntity job) {
		String jobClass = job.getJobClass();
		if (!quartzTaskRegistry.isAllowed(jobClass)) {
			throw new SysJobException(JOB_CLASS_INVALID, "not in whitelist");
		}

		try {
			Class.forName(CharSequenceUtil.nullToEmpty(jobClass));
		}
		catch (ClassNotFoundException exception) {
			throw new SysJobException(JOB_CLASS_INVALID, exception.getMessage());
		}

		builtinJobParamsValidator.validateIfPresent(job);
	}

}
