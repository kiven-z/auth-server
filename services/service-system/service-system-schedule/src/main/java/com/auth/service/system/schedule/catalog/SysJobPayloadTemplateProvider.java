package com.auth.service.system.schedule.catalog;

import com.auth.service.system.schedule.model.enums.SysJobTaskType;

/**
 * 任务载荷模板 SPI（由各业务模块按 taskType 提供实现）
 *
 * @author Bunny
 */
public interface SysJobPayloadTemplateProvider {

	/**
	 * 支持的任务类型
	 * @return 任务类型
	 */
	SysJobTaskType taskType();

	/**
	 * 构建该任务类型的 payload 模板
	 * @return 载荷模板（具体类型由实现方决定）
	 */
	Object buildTemplatePayload();

}
