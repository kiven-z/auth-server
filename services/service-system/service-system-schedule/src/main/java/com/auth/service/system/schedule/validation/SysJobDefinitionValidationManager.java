package com.auth.service.system.schedule.validation;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_TASK_TYPE_UNSUPPORTED;

/**
 * 任务定义校验管理器
 *
 * @author Bunny
 */
@Component
public class SysJobDefinitionValidationManager {

	private final Map<SysJobTaskType, SysJobTaskTypeDefinitionValidator> validators;

	public SysJobDefinitionValidationManager(List<SysJobTaskTypeDefinitionValidator> validatorList) {
		this.validators = new EnumMap<>(SysJobTaskType.class);
		for (SysJobTaskTypeDefinitionValidator validator : validatorList) {
			this.validators.put(validator.taskType(), validator);
		}
	}

	/**
	 * 归一化并执行任务定义校验
	 * @param job 任务定义
	 */
	public void normalizeAndValidate(SysJobEntity job) {
		SysJobTaskType resolvedTaskType = SysJobTaskType.resolve(job.getTaskType());
		job.setTaskType(resolvedTaskType.name());

		if (CharSequenceUtil.isBlank(job.getHandlerCode())) {
			job.setHandlerCode(resolvedTaskType.defaultHandlerCode());
		}

		SysJobTaskTypeDefinitionValidator validator = validators.get(resolvedTaskType);
		if (validator == null) {
			throw new SysJobException(JOB_TASK_TYPE_UNSUPPORTED, resolvedTaskType.name());
		}
		validator.validate(job);
	}

}
