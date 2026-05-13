package com.auth.service.system.log.support;

import com.auth.module.security.contract.event.OperationLogPayload;
import com.auth.module.security.contract.spi.OperationLogHandler;
import com.auth.service.system.log.service.LogOperationAppendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 将操作日志负载异步写入 {@code log_operation}
 *
 * @author Bunny
 */
@Slf4j
@Component
public class OperationLogPersistHandler implements OperationLogHandler {

	private final LogOperationAppendService logOperationAppendService;

	public OperationLogPersistHandler(LogOperationAppendService logOperationAppendService) {
		this.logOperationAppendService = logOperationAppendService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Async
	public void handle(OperationLogPayload payload) {
		try {
			logOperationAppendService.append(payload);
		}
		catch (RuntimeException ex) {
			log.error("Failed to persist operation log, uri={}, module={}", payload.getRequestUri(),
					payload.getModule(), ex);
		}
	}

}
