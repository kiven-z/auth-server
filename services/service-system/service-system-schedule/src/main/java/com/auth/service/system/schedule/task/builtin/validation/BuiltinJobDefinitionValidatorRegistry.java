package com.auth.service.system.schedule.task.builtin.validation;

import com.auth.service.system.schedule.model.entity.SysJobEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 内置 Job {@code job_params} 校验注册中心
 *
 * @author Bunny
 */
@Component
public class BuiltinJobDefinitionValidatorRegistry {

	private final Map<String, BuiltinJobDefinitionValidator> validators;

	public BuiltinJobDefinitionValidatorRegistry(List<BuiltinJobDefinitionValidator> validatorList) {
		this.validators = new HashMap<>();
		for (BuiltinJobDefinitionValidator validator : validatorList) {
			this.validators.put(validator.jobClass(), validator);
		}
	}

	/**
	 * 若 {@code job_class} 对应内置校验器则校验 {@code job_params}
	 * @param job 任务定义
	 */
	public void validateIfPresent(SysJobEntity job) {
		BuiltinJobDefinitionValidator validator = validators.get(job.getJobClass());
		if (validator != null) {
			validator.validateJobParams(job.getJobParams());
		}
	}

}
