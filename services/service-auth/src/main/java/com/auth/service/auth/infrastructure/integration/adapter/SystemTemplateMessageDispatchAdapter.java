package com.auth.service.auth.infrastructure.integration.adapter;

import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.auth.application.port.login.TemplateMessageDispatchPort;
import com.auth.service.auth.infrastructure.integration.SystemSecurityIngestFeignClient;
import org.springframework.stereotype.Repository;

/**
 * 模板消息发送适配器：经 Feign 委托 system 服务。
 *
 * @author Bunny
 */
@Repository
public class SystemTemplateMessageDispatchAdapter implements TemplateMessageDispatchPort {

	private final SystemSecurityIngestFeignClient systemSecurityIngestFeignClient;

	public SystemTemplateMessageDispatchAdapter(SystemSecurityIngestFeignClient systemSecurityIngestFeignClient) {
		this.systemSecurityIngestFeignClient = systemSecurityIngestFeignClient;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendByTemplate(TemplateMessageCommand command) {
		systemSecurityIngestFeignClient.sendByTemplate(command);
	}

}
