package com.auth.service.system.schedule.service;

/**
 * 任务载荷模板服务（按 taskType 解析标准 payload 骨架）
 *
 * @author Bunny
 */
public interface SysJobPayloadTemplateService {

	/**
	 * 获取指定任务类型的 payload 模板
	 * @param taskType 任务类型
	 * @return 载荷模板
	 */
	Object resolvePayloadTemplate(String taskType);

}
