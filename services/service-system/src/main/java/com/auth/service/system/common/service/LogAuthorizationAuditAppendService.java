package com.auth.service.system.common.service;

import com.auth.module.security.contract.event.OperationLogPayload;
import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;

/**
 * 将授权审计事件追加写入
 *
 * @author Bunny
 */
public interface LogAuthorizationAuditAppendService {

	/**
	 * 持久化一条授权审计记录
	 * @param event 授权审计负载
	 */
	void append(SecurityAuthorizationAuditPayloadEvent event);

	/**
	 * 添加一条操作日志记录
	 * @param payload 操作日志负载
	 */
	void append(OperationLogPayload payload);

}
