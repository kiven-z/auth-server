package com.auth.service.system.log.service;

import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;

/**
 * 将授权审计事件追加写入 {@code log_authorization_audit}（供本地 SPI 与对内 HTTP 共用）。
 *
 * @author Bunny
 */
public interface LogAuthorizationAuditAppendService {

	/**
	 * 持久化一条授权审计记录。
	 * @param event 授权审计负载
	 */
	void append(SecurityAuthorizationAuditPayloadEvent event);

}
