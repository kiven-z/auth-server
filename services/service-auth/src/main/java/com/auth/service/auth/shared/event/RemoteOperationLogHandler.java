package com.auth.service.auth.shared.event;

import cn.hutool.core.util.BooleanUtil;
import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.dto.OperationLogIngestRequest;
import com.auth.module.security.contract.event.OperationLogPayloadEvent;
import com.auth.module.security.contract.spi.OperationLogHandler;
import com.auth.service.auth.shared.integration.SystemSecurityIngestFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static com.auth.common.web.resttemplate.FeignUtil.isSuccess;

/**
 * 将操作日志负载异步委托给 service-system 通过 Feign 上报（没有本地 {@code log_operation} 访问权限）
 *
 * @author Bunny
 */
@Slf4j
@Component
public class RemoteOperationLogHandler implements OperationLogHandler {

	private final SystemSecurityIngestFeignClient systemSecurityIngestFeignClient;

	public RemoteOperationLogHandler(SystemSecurityIngestFeignClient systemSecurityIngestFeignClient) {
		this.systemSecurityIngestFeignClient = systemSecurityIngestFeignClient;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Async
	public void handle(OperationLogPayloadEvent payload) {
		OperationLogIngestRequest body = OperationLogIngestRequest.fromPayload(payload);
		try {
			Result<Void> result = systemSecurityIngestFeignClient.appendOperationLog(body);
			if (BooleanUtil.isFalse(isSuccess(result))) {
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
