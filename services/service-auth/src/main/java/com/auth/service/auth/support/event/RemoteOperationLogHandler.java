package com.auth.service.auth.support.event;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.dto.OperationLogIngestRequest;
import com.auth.module.security.contract.event.OperationLogPayload;
import com.auth.module.security.contract.spi.OperationLogHandler;
import com.auth.service.auth.support.integration.SystemAuthorizationAuditFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 将操作日志负载异步委托给 service-system 通过 Feign 上报（没有本地 {@code log_operation} 访问权限）。
 *
 * @author Bunny
 */
@Slf4j
@Component
public class RemoteOperationLogHandler implements OperationLogHandler {

	private final SystemAuthorizationAuditFeignClient authorizationAuditFeignClient;

	public RemoteOperationLogHandler(SystemAuthorizationAuditFeignClient authorizationAuditFeignClient) {
		this.authorizationAuditFeignClient = authorizationAuditFeignClient;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Async
	public void handle(OperationLogPayload payload) {
		OperationLogIngestRequest body = OperationLogIngestRequest.fromPayload(payload);
		try {
			Result<Void> result = authorizationAuditFeignClient.appendRecord(body);
			// FIXME 使用 isOk(result); FeignUtil.isOk()
			if (result == null || !Integer.valueOf(Result.SUCCESS_CODE).equals(result.getCode())) {
				log.error("Operation log Feign returned non-success: code={}, message={}, requestUri={}, module={}",
						result != null ? result.getCode() : null, result != null ? result.getMessage() : null,
						payload.getRequestUri(), payload.getModule());
			}
		}
		catch (RuntimeException ex) {
			log.error("Failed to submit operation log via Feign, requestUri={}, module={}", payload.getRequestUri(),
					payload.getModule(), ex);
		}
	}

}
