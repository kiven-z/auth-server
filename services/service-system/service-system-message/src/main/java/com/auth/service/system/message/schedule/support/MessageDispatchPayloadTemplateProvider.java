package com.auth.service.system.message.schedule.support;

import com.auth.service.system.message.schedule.model.MessageDispatchPayload;
import com.auth.service.system.schedule.catalog.SysJobPayloadTemplateProvider;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import org.springframework.stereotype.Component;

/**
 * 消息派发任务 payload 模板提供者
 *
 * @author Bunny
 */
@Component
public class MessageDispatchPayloadTemplateProvider implements SysJobPayloadTemplateProvider {

	private final MessageDispatchCommandParser messageDispatchCommandParser;

	public MessageDispatchPayloadTemplateProvider(MessageDispatchCommandParser messageDispatchCommandParser) {
		this.messageDispatchCommandParser = messageDispatchCommandParser;
	}

	@Override
	public SysJobTaskType taskType() {
		return SysJobTaskType.MESSAGE_DISPATCH;
	}

	@Override
	public MessageDispatchPayload buildTemplatePayload() {
		return messageDispatchCommandParser.buildTemplatePayload();
	}

}
