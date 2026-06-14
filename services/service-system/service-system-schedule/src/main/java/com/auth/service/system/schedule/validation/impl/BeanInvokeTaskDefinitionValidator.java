package com.auth.service.system.schedule.validation.impl;

import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.validation.SysJobTaskTypeDefinitionValidator;
import com.auth.service.system.schedule.validation.SysJobTaskTypeFieldRules;
import org.springframework.stereotype.Component;

/**
 * Bean 调用任务定义校验器
 *
 * @author Bunny
 */
@Component
public class BeanInvokeTaskDefinitionValidator implements SysJobTaskTypeDefinitionValidator {

	@Override
	public SysJobTaskType taskType() {
		return SysJobTaskType.BEAN_INVOKE;
	}

	@Override
	public void validate(SysJobEntity job) {
		SysJobTaskTypeFieldRules
			.detect(SysJobTaskType.BEAN_INVOKE, job.getJobClass(), job.getInvokeTarget(), job.getPayloadJson(), false)
			.ifPresent(SysJobTaskTypeFieldRules::throwIfPresent);
	}

}
