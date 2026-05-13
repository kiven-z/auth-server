package com.auth.service.auth.support.integration;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.dto.AuthorizationAuditIngestRequest;
import com.auth.module.security.contract.dto.OperationLogIngestRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调用 system 服务，将授权审计写入 {@code log_authorization_audit}
 *
 * @author Bunny
 */
@FeignClient(name = "service-system", contextId = "systemAuthorizationAudit", path = "/api/system/inner")
public interface SystemAuthorizationAuditFeignClient {

	/**
	 * 上报一条授权审计记录
	 * @param body 传输负载
	 * @return 统一结果
	 */
	@PostMapping("/authorization-audit/records")
	Result<Void> appendRecord(@RequestBody AuthorizationAuditIngestRequest body);

	/**
	 * Appends one operation log record.
	 * @param body wire body
	 * @return unified result
	 */
	@PostMapping("/operation-log/records")
	Result<Void> appendRecord(@RequestBody OperationLogIngestRequest body);

}
