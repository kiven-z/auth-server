package com.auth.service.system.schedule.validation;

import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;

/**
 * 任务定义校验器（按任务类型拆分，可选注册）
 * <p>
 * 表单层已覆盖的字段互斥规则不在此重复；各模块仅注册注解无法完成的校验（如白名单、载荷解析） MESSAGE_DISPATCH 由
 * {@code service-system-message} 提供实现
 * </p>
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
