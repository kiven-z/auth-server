package com.auth.service.example.feign;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.dto.AuthorizationAuditIngestRequest;
import com.auth.module.security.contract.dto.OperationLogIngestRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调用 system 服务内部接口，将授权审计与操作日志上报落库。
 *
 * @author Bunny
 */
@FeignClient(name = "service-system", contextId = "systemSecurityIngest", path = "/api/system/inner")
public interface SystemSecurityIngestFeignClient {

	/**
	 * 上报一条授权审计记录
	 * @param body 传输负载
	 * @return 统一结果
	 */
	@PostMapping("/authorization-audit/records")
	Result<Void> appendAuthorizationAudit(@RequestBody AuthorizationAuditIngestRequest body);

	/**
	 * 上报一条操作日志记录
	 * @param body 传输负载
	 * @return 统一结果
	 */
	@PostMapping("/operation-log/records")
	Result<Void> appendOperationLog(@RequestBody OperationLogIngestRequest body);

}
