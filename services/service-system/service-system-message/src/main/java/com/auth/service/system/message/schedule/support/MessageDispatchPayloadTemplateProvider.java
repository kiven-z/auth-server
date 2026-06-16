package com.auth.service.system.message.schedule.support;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.message.schedule.model.MessageDispatchPayload;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.support.catalog.SysJobPayloadTemplateProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 消息派发任务 payload 模板提供者
 *
 * @author Bunny
 */
@Component
public class MessageDispatchPayloadTemplateProvider implements SysJobPayloadTemplateProvider {

	@Override
	public SysJobTaskType taskType() {
		return SysJobTaskType.MESSAGE_DISPATCH;
	}

	@Override
	public MessageDispatchPayload buildTemplatePayload() {
		MessageDispatchPayload payload = new MessageDispatchPayload();
		payload.setVersion(MessageDispatchPayload.VERSION_1);
		payload.setCommand(TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("LOGIN_NOTICE")
			.targets(List.of("dev@example.com"))
			.build());
		return payload;
	}

}
