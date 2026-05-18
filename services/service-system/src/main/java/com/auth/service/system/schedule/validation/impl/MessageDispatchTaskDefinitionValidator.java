package com.auth.service.system.schedule.validation.impl;

import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.support.MessageDispatchCommandParser;
import com.auth.service.system.schedule.validation.SysJobTaskTypeDefinitionValidator;
import org.springframework.stereotype.Component;

/**
 * 消息派发任务定义校验器
 *
 * @author Bunny
 */
@Component
public class MessageDispatchTaskDefinitionValidator implements SysJobTaskTypeDefinitionValidator {

	private final MessageDispatchCommandParser messageDispatchCommandParser;

	public MessageDispatchTaskDefinitionValidator(MessageDispatchCommandParser messageDispatchCommandParser) {
		this.messageDispatchCommandParser = messageDispatchCommandParser;
	}

	@Override
	public SysJobTaskType taskType() {
		return SysJobTaskType.MESSAGE_DISPATCH;
	}

	@Override
	public void validate(SysJobEntity job) {
		messageDispatchCommandParser.parseAndValidate(job.getPayloadJson());
	}

}
