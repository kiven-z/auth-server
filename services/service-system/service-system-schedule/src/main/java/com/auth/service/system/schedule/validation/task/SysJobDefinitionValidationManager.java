package com.auth.service.system.schedule.validation.task;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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
		//
		String taskType = job.getTaskType();
		SysJobTaskType resolvedTaskType = SysJobTaskType.resolve(taskType);
		job.setTaskType(resolvedTaskType.name());

		//
		if (CharSequenceUtil.isBlank(job.getHandlerCode())) {
			String handlerCode = resolvedTaskType.defaultHandlerCode();
			job.setHandlerCode(handlerCode);
		}

		//
		SysJobTaskTypeDefinitionValidator validator = validators.get(resolvedTaskType);
		if (validator != null) {
			validator.validate(job);
		}
	}

}
