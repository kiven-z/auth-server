package com.auth.service.auth.support.integration;

import com.auth.common.core.model.response.Result;
import com.auth.module.message.api.command.TemplateMessageCommand;
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
@FeignClient(name = "service-system", contextId = "systemSecurityIngest", path = "/api/system")
public interface SystemSecurityIngestFeignClient {

	/**
	 * 上报一条授权审计记录
	 * @param body 传输负载
	 * @return 统一结果
	 */
	@PostMapping("inner/authorization-audit/records")
	Result<Void> appendAuthorizationAudit(@RequestBody AuthorizationAuditIngestRequest body);

	/**
	 * 上报一条操作日志记录
	 * @param body 传输负载
	 * @return 统一结果
	 */
	@PostMapping("inner/operation-log/records")
	Result<Void> appendOperationLog(@RequestBody OperationLogIngestRequest body);

	/**
	 * 按模板发送消息（多渠道统一入口）
	 * @param command 模板化发送命令
	 * @return 统一结果
	 */
	@PostMapping("message/send")
	Result<String> sendByTemplate(@RequestBody TemplateMessageCommand command);

}
