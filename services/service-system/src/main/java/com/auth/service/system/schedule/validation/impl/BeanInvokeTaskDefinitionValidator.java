package com.auth.service.system.schedule.validation.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.validation.SysJobTaskTypeDefinitionValidator;
import org.springframework.stereotype.Component;

import static com.auth.service.system.common.exception.code.schedule.SystemScheduleResultCode.JOB_MODE_BEAN_REQUIRES_INVOKE_TARGET;

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
		if (CharSequenceUtil.isBlank(job.getInvokeTarget())) {
			throw new SystemBusinessException(JOB_MODE_BEAN_REQUIRES_INVOKE_TARGET);
		}
	}

}
