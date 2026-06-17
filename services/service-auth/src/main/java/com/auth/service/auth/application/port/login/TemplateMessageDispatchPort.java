package com.auth.service.auth.application.port.login;

import com.auth.module.message.api.command.TemplateMessageCommand;

/**
 * 模板消息发送端口：委托 system 服务多渠道投递。
 *
 * @author Bunny
 */
public interface TemplateMessageDispatchPort {

	/**
	 * 按模板发送消息
	 * @param command 模板化发送命令
	 */
	void sendByTemplate(TemplateMessageCommand command);

}
