package com.auth.service.system.message.schedule.support;

import com.auth.service.system.message.schedule.model.MessageDispatchPayload;
import com.auth.service.system.schedule.catalog.MessageDispatchPayloadTemplateProvider;
import org.springframework.stereotype.Component;

/**
 * 消息调度 payload 模板 SPI 实现
 *
 * @author Bunny
 */
@Component
public class MessageDispatchPayloadTemplateProviderImpl implements MessageDispatchPayloadTemplateProvider {

	private final MessageDispatchCommandParser messageDispatchCommandParser;

	public MessageDispatchPayloadTemplateProviderImpl(MessageDispatchCommandParser messageDispatchCommandParser) {
		this.messageDispatchCommandParser = messageDispatchCommandParser;
	}

	@Override
	public MessageDispatchPayload buildTemplatePayload() {
		return messageDispatchCommandParser.buildTemplatePayload();
	}

}
