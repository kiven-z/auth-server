package com.auth.service.system.log.service;

import com.auth.module.security.contract.event.OperationLogPayload;

/**
 * 将操作日志负载追加写入 {@code log_operation}
 *
 * @author Bunny
 */
public interface LogOperationAppendService {

	/**
	 * 追加一条操作日志
	 * @param payload 契约负载
	 */
	void append(OperationLogPayload payload);

}
