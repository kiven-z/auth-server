package com.auth.service.system.schedule.catalog;

/**
 * 消息调度 payload 模板 SPI（由 message 模块提供实现）
 *
 * @author Bunny
 */
public interface MessageDispatchPayloadTemplateProvider {

	/**
	 * 构建标准消息调度载荷模板
	 * @return 载荷模板（具体类型由实现方决定）
	 */
	Object buildTemplatePayload();

}
