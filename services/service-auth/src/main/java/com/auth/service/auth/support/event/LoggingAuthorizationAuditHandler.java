package com.auth.service.auth.support.event;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.dto.AuthorizationAuditIngestRequest;
import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.module.security.contract.spi.AuthorizationAuditHandler;
import com.auth.service.auth.support.integration.SystemAuthorizationAuditFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 认证审计处理器：经 Feign 调用 system 将授权决策事件持久化
 *
 * @author Bunny
 */
@Slf4j
@Component
public class LoggingAuthorizationAuditHandler implements AuthorizationAuditHandler {

	private final SystemAuthorizationAuditFeignClient systemAuthorizationAuditFeignClient;

	public LoggingAuthorizationAuditHandler(SystemAuthorizationAuditFeignClient systemAuthorizationAuditFeignClient) {
		this.systemAuthorizationAuditFeignClient = systemAuthorizationAuditFeignClient;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Async
	public void handle(SecurityAuthorizationAuditPayloadEvent event) {
		AuthorizationAuditIngestRequest body = AuthorizationAuditIngestRequest.fromEvent(event);
		try {
			Result<Void> result = systemAuthorizationAuditFeignClient.appendRecord(body);
			// FIXME 使用 isOk(result); FeignUtil.isOk()
			if (result == null || !Integer.valueOf(Result.SUCCESS_CODE).equals(result.getCode())) {
				log.error(
						"Authorization audit Feign returned non-success: code={}, message={}, requestUri={}, userId={}",
						result != null ? result.getCode() : null, result != null ? result.getMessage() : null,
						event.getRequestUri(), event.getUserId());
			}
		}
		catch (RuntimeException ex) {
			log.error("Failed to submit authorization audit via Feign, requestUri={}, userId={}", event.getRequestUri(),
					event.getUserId(), ex);
		}
	}

}
