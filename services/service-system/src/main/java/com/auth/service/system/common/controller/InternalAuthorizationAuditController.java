package com.auth.service.system.common.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.module.security.contract.dto.AuthorizationAuditIngestRequest;
import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.service.system.log.service.LogAuthorizationAuditAppendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对内接口：接收其他微服务上报的授权审计事件并落库。
 *
 * @author Bunny
 */
@Tag(name = "系统服务-内部-授权审计")
@RequestMapping("/api/system/inner/authorization-audit")
@RestController
public class InternalAuthorizationAuditController {

	private final LogAuthorizationAuditAppendService logAuthorizationAuditAppendService;

	public InternalAuthorizationAuditController(LogAuthorizationAuditAppendService logAuthorizationAuditAppendService) {
		this.logAuthorizationAuditAppendService = logAuthorizationAuditAppendService;
	}

	/**
	 * 追加一条权限决策审计记录。
	 * @param body 传输负载
	 * @return 统一结果
	 */
	@InternalApi
	@Operation(summary = "追加授权审计记录", description = "供 service-auth 等经服务发现上报，写入 log_authorization_audit")
	@PostMapping("/records")
	public Result<Void> appendRecord(@RequestBody AuthorizationAuditIngestRequest body) {
		SecurityAuthorizationAuditPayloadEvent event = body.toPayloadEvent();
		logAuthorizationAuditAppendService.append(event);
		return Result.success();
	}

}
