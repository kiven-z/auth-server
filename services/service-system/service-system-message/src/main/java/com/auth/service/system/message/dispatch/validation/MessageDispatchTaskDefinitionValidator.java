package com.auth.service.system.message.dispatch.validation;

import com.auth.service.system.message.dispatch.support.MessageDispatchCommandParser;
import com.auth.service.system.schedule.model.entity.JobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.validation.task.SysJobTaskTypeDefinitionValidator;

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysJobTaskType taskType() {
		return SysJobTaskType.MESSAGE_DISPATCH;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate(JobEntity job) {
		String payloadJson = job.getPayloadJson();
		messageDispatchCommandParser.parseAndValidate(payloadJson);
	}

}
