package com.auth.service.system.schedule.validation;

import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;

/**
 * 任务定义校验器（按任务类型拆分）
 *
 * @author Bunny
 */
public interface SysJobTaskTypeDefinitionValidator {

	/**
	 * 支持的任务类型
	 * @return 任务类型
	 */
	SysJobTaskType taskType();

	/**
	 * 校验任务定义
	 * @param job 任务定义
	 */
	void validate(SysJobEntity job);

}
