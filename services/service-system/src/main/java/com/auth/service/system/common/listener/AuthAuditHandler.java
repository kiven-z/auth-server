package com.auth.service.system.common.listener;

import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.module.security.contract.spi.AuthorizationAuditHandler;
import com.auth.service.system.common.service.SecurityIngestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 认证审计处理器：将授权决策事件持久化至 {@code log_authorization_audit}。
 *
 * @author Bunny
 */
@Slf4j
@Component
public class AuthAuditHandler implements AuthorizationAuditHandler {

	private final SecurityIngestService securityIngestService;

	public AuthAuditHandler(SecurityIngestService securityIngestService) {
		this.securityIngestService = securityIngestService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(SecurityAuthorizationAuditPayloadEvent event) {
		try {
			securityIngestService.append(event);
		}
		catch (RuntimeException ex) {
			log.error("Failed to persist authorization audit event", ex);
		}
	}

}
