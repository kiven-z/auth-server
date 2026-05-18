package com.auth.service.system.schedule.handler;

import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.support.MessageDispatchCommandParser;
import com.auth.service.system.schedule.task.mode.message.MessageDispatchEntryJob;
import org.quartz.Job;
import org.springframework.stereotype.Component;

/**
 * 消息派发任务处理器
 *
 * @author Bunny
 */
@Component
public class MessageDispatchSysJobTaskHandler implements SysJobTaskHandler {

	private final MessageDispatchCommandParser commandParser;

	public MessageDispatchSysJobTaskHandler(MessageDispatchCommandParser commandParser) {
		this.commandParser = commandParser;
	}

	@Override
	public String handlerCode() {
		return "messageDispatchHandler";
	}

	@Override
	public SysJobTaskType taskType() {
		return SysJobTaskType.MESSAGE_DISPATCH;
	}

	@Override
	public void validate(SysJobEntity job) {
		commandParser.parseAndValidate(job.getPayloadJson());
	}

	@Override
	public Class<? extends Job> resolveEntryJobClass(boolean concurrent) {
		return concurrent ? MessageDispatchEntryJob.class : MessageDispatchEntryJob.Disallow.class;
	}

}
