package com.auth.service.system.common.listener;

import com.alibaba.fastjson2.JSON;
import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.module.security.contract.spi.AuthorizationAuditHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 认证审计处理器
 *
 * @author Bunny
 */
@Slf4j
@Component
public class AuthAuditHandler implements AuthorizationAuditHandler {

	@Override
	@Async
	public void handle(SecurityAuthorizationAuditPayloadEvent event) {
		log.info("================System================");
		log.info("{}", JSON.toJSONString(event));
		log.info("================System================");
	}

}