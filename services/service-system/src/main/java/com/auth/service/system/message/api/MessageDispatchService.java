package com.auth.service.system.message.api;

import com.auth.module.message.command.TemplateMessageCommand;

/**
 * 消息发送编排：按渠道路由到对应 {@link com.auth.module.message.spi.MessageSender}
 *
 * @author Bunny
 */
public interface MessageDispatchService {

	/**
	 * 按模板发送消息（自动路由渠道）
	 * @param command 模板化发送命令
	 */
	void sendByTemplate(TemplateMessageCommand command);

}
