package com.auth.service.system.common.listener;

import com.auth.module.security.contract.event.OperationLogPayload;
import com.auth.module.security.contract.spi.OperationLogHandler;
import com.auth.service.system.common.service.LogAuthorizationAuditAppendService;
import com.auth.service.system.log.service.LogOperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 将操作日志负载异步委托给 {@link LogOperationService#appendForAudit} 写入 {@code log_operation}。
 *
 * @author Bunny
 */
@Slf4j
@Component
public class OperationLogPersistHandler implements OperationLogHandler {

	private final LogAuthorizationAuditAppendService logAuthorizationAuditAppendService;

	public OperationLogPersistHandler(LogAuthorizationAuditAppendService logAuthorizationAuditAppendService) {
		this.logAuthorizationAuditAppendService = logAuthorizationAuditAppendService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Async
	public void handle(OperationLogPayload payload) {
		try {
			logAuthorizationAuditAppendService.append(payload);
		}
		catch (RuntimeException ex) {
			log.error("Failed to persist operation log, uri={}, module={}", payload.getRequestUri(),
					payload.getModule(), ex);
		}
	}

}
