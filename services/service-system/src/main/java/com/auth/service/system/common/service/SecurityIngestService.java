package com.auth.service.system.common.service;

import com.auth.module.security.contract.event.OperationLogPayloadEvent;
import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;

/**
 * 接收内部上报的安全相关负载并落库（授权审计、操作日志等）。
 *
 * @author Bunny
 */
public interface SecurityIngestService {

	/**
	 * 持久化一条授权审计记录
	 * @param event 授权审计负载
	 */
	void append(SecurityAuthorizationAuditPayloadEvent event);

	/**
	 * 持久化一条操作日志记录
	 * @param payload 操作日志负载
	 */
	void append(OperationLogPayloadEvent payload);

}
